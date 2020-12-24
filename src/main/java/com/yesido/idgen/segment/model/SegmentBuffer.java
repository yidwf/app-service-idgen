package com.yesido.idgen.segment.model;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.util.Assert;

public class SegmentBuffer {

    private String key;
    private Segment[] segments; // 双buffer
    private volatile int currentPos = 0; // 当前正在使用的segment的index
    private volatile boolean nextReady = false; // 下一个segment是否处于可切换状态
    private volatile boolean init = false; // 是否初始化完成
    private final AtomicBoolean threadRunning; // 线程是否在运行中
    private final ReadWriteLock lock;

    private volatile int step;
    private volatile int minStep;
    private volatile long updateTimestamp;

    public SegmentBuffer(String key) {
        Assert.hasLength(key, "key can not be null");
        this.key = key;
        segments = new Segment[]{new Segment(this), new Segment(this)};
        threadRunning = new AtomicBoolean(false);
        lock = new ReentrantReadWriteLock();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Segment[] getSegments() {
        return segments;
    }

    public void setSegments(Segment[] segments) {
        this.segments = segments;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }

    public boolean isNextReady() {
        return nextReady;
    }

    public void setNextReady(boolean nextReady) {
        this.nextReady = nextReady;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getMinStep() {
        return minStep;
    }

    public void setMinStep(int minStep) {
        this.minStep = minStep;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public AtomicBoolean getThreadRunning() {
        return threadRunning;
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    /**
     * 获取当前segment
     * 
     * @author yesido
     * @date 2020年12月18日 下午3:42:19
     * @return
     */
    public Segment getCurrent() {
        return segments[currentPos];
    }

    /**
     * 获取下次要切换segment的index
     * 
     * @author yesido
     * @date 2020年12月18日 下午3:42:34
     * @return
     */
    public int nextPos() {
        return (currentPos + 1) % 2;
    }

    public void switchPos() {
        currentPos = nextPos();
    }

    public Lock rLock() {
        return lock.readLock();
    }

    public Lock wLock() {
        return lock.writeLock();
    }

    @Override
    public String toString() {
        return "SegmentBuffer [key=" + key + ", segments=" + Arrays.toString(segments) + ", currentPos=" + currentPos + ", nextReady=" + nextReady
                + ", init=" + init + ", threadRunning=" + threadRunning + ", step=" + step + ", minStep=" + minStep + ", updateTimestamp="
                + updateTimestamp + "]";
    }

}
