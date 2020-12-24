package com.yesido.idgen.segment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yesido.idgen.segment.dao.LeafAllocDao;
import com.yesido.idgen.segment.exception.SegmentIsNotInitException;
import com.yesido.idgen.segment.exception.SegmentKeyNotExistException;
import com.yesido.idgen.segment.exception.SegmentNotReadyException;
import com.yesido.idgen.segment.model.LeafAlloc;
import com.yesido.idgen.segment.model.Segment;
import com.yesido.idgen.segment.model.SegmentBuffer;

@Service("segmentIDGen")
public class SegmentIDGen extends AbstractSegmentIDGen {
    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentIDGen.class);
    /**
     * 一个Segment维持时间为15分钟，用来衡量id的获取频率
     */
    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;
    /**
     * 最大步长不超过1,000,000
     */
    private static final int MAX_STEP = 1000000;
    private volatile boolean init = false;
    private Map<String, SegmentBuffer> cache = new ConcurrentHashMap<String, SegmentBuffer>();
    private ExecutorService service = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    @Autowired
    private LeafAllocDao leafAllocDao;

    @Override
    public long nextId(String key) {
        if (!init) {
            throw new SegmentIsNotInitException("Not init", key);
        }
        if (!cache.containsKey(key)) {
            throw new SegmentKeyNotExistException("Key not exist", key);
        }
        SegmentBuffer buffer = cache.get(key);
        if (!buffer.isInit()) {
            synchronized (buffer) {
                if (!buffer.isInit()) {
                    try {
                        updateSegment(key, buffer.getCurrent());
                        LOGGER.info("首次初始化buffer. key={} {}", key, buffer.getCurrent());
                        buffer.setInit(true);
                    } catch (Exception e) {
                        LOGGER.warn("首次初始化buffer {} exception", buffer.getCurrent(), e);
                    }
                }
            }
        }
        return getFromBuffer(buffer);
    }

    private void updateSegment(String key, Segment segment) {
        // LOGGER.info("更新数据库-updateSegment，自增key：{} -> {}", key, segment);
        SegmentBuffer buffer = segment.getBuffer();
        LeafAlloc alloc;
        if (!buffer.isInit()) {
            // 首次init
            alloc = leafAllocDao.updateMaxIdAndGetLeafAlloc(key);
            LOGGER.info("更新数据库-init，自增key：{}", key);
            buffer.setStep(alloc.getStep());
            buffer.setMinStep(alloc.getStep());//leafAlloc中的step为DB中的step
        } else if (buffer.getUpdateTimestamp() == 0) {
            // init之后的第一次更新
            alloc = leafAllocDao.updateMaxIdAndGetLeafAlloc(key);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(alloc.getStep());
            buffer.setMinStep(alloc.getStep());//leafAlloc中的step为DB中的step
            LOGGER.info("更新数据库-first，自增key：{}", key);
        } else {
            // 两次更新时间差
            long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
            int nextStep = buffer.getStep();
            if (duration < SEGMENT_DURATION) {
                // 两次更新的时间时间差小于阈值，表示这段时间内获取id的需求较大，增大步长，预先缓存更多id
                if (nextStep * 2 > MAX_STEP) {
                    // ignore
                } else {
                    nextStep = nextStep * 2;
                }
            } else if (duration < SEGMENT_DURATION * 2) {
                // ignore
            } else {
                // 获取id的需求较小，减少步长
                nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
            }
            LeafAlloc temp = new LeafAlloc();
            temp.setBiz_tag(key);
            temp.setStep(nextStep);
            alloc = leafAllocDao.updateMaxIdByCustomStepAndGetLeafAlloc(temp);
            LOGGER.info("更新数据库-两次，自增key：{},step:{}", key, nextStep);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(nextStep);
            buffer.setMinStep(alloc.getStep()); //alloc的step为DB中的step
        }
        long currentId = alloc.getMax_id() - buffer.getStep();
        segment.getCurrentValue().set(currentId);
        segment.setMax(alloc.getMax_id());
        segment.setStep(buffer.getStep());
        LOGGER.info("更新数据库-结果 key={}, {}", key, segment);
    }

    private long getFromBuffer(SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                checkNextSegment(buffer, segment);
                long id = segment.getCurrentValue().getAndIncrement();
                if (id < segment.getMax()) {
                    return id;
                }
            } finally {
                buffer.rLock().unlock();
            }
            waitAndSleep(buffer);
            buffer.wLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                long id = segment.getCurrentValue().getAndIncrement();
                if (id < segment.getMax()) {
                    return id;
                }
                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    LOGGER.error("Both two segments in {} are not ready!", buffer);
                    throw new SegmentNotReadyException("Two segments are not ready", buffer.getKey());
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    private void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if (roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    LOGGER.warn("Thread {} Interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
    }

    private void checkNextSegment(SegmentBuffer buffer, final Segment segment) {
        if (!buffer.isNextReady() && segment.getIdle() < 0.9 * segment.getStep()
                && buffer.getThreadRunning().compareAndSet(false, true)) {
            service.execute(() -> {
                Segment next = buffer.getSegments()[buffer.nextPos()];
                boolean updateOk = false;
                try {
                    LOGGER.info("更新数据库-检测下一个segment，当前segment：{}，next:{}", segment, next);
                    updateSegment(buffer.getKey(), next);
                    updateOk = true;
                } catch (Exception e) {
                    LOGGER.warn("UpdateSegment {} exception", next, e);
                } finally {
                    if (updateOk) {
                        buffer.wLock().lock();
                        buffer.setNextReady(true);
                        buffer.getThreadRunning().set(false);
                        buffer.wLock().unlock();
                    } else {
                        buffer.getThreadRunning().set(false);
                    }
                }
            });
        }
    }

    @Override
    public boolean init() {
        updateCache();
        this.init = true;
        startUpdateCacheJob();
        return true;
    }

    private void updateCache() {
        List<String> tags = leafAllocDao.listAllBizTags(); // 数据库中所有的tag
        if (tags == null || tags.isEmpty()) {
            return;
        }
        List<String> cacheTags = new ArrayList<String>(cache.keySet()); // 缓存中的tag
        // 新的tag
        List<String> newTags = tags.stream().filter(t -> !cacheTags.contains(t)).collect(Collectors.toList());
        for (String tag : newTags) {
            // 还没有init
            SegmentBuffer buffer = new SegmentBuffer(tag);
            cache.put(tag, buffer);
            LOGGER.info("添加buffer：{}", buffer);
        }
        // 失效的tag
        List<String> deleteTags = cacheTags.stream().filter(t -> !tags.contains(t)).collect(Collectors.toList());
        for (String tag : deleteTags) {
            cache.remove(tag);
            LOGGER.info("异步buffer：{}", tag);
        }
    }

    private void startUpdateCacheJob() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("update-Cache-thread");
                t.setDaemon(true);
                return t;
            }
        });
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    updateCache();
                } catch (Exception e) {
                    LOGGER.warn("update cache exception", e);
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

}
