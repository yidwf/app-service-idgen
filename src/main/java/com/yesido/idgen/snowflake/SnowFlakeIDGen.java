package com.yesido.idgen.snowflake;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yesido.idgen.snowflake.exception.SnowflakeClockBackException;

/**
 * Snowflake生成器
 * 
 * @author yesido
 * @date 2020年12月17日 上午11:46:42
 */
public class SnowFlakeIDGen extends AbstractSnowFlakeIDGen {
    private static Logger LOGGER = LoggerFactory.getLogger(SnowFlakeIDGen.class);
    /**
     * 初始时间戳:2018-01-01 00:00:00<br>
     * 一经定义不可修改
     */
    private final static long beginTs = 1483200000000L;
    private final static int workerIdBits = 10; // 进程位数
    private final static int sequenceBits = 12; // sequence位数
    private final static int timeBits = workerIdBits + sequenceBits; // 时间位数
    private final static int sequenceMax = (1 << sequenceBits) - 1; // sequence最大值
    private final static int clockBack = 5; // 时钟回拨可接受毫秒数
    private long workerId; // 进程id
    private volatile AtomicInteger seq = new AtomicInteger(1); // 递增序号
    private volatile long lastTs = 1514736000000L;

    public SnowFlakeIDGen(long workerId) {
        if (workerId < 1 || workerId > ((1 << workerIdBits) - 1)) {
            throw new RuntimeException("进程ID超出范围，最小：1，最大：" + ((1 << workerIdBits) - 1));
        }
        this.workerId = workerId;
        LOGGER.info("SnowFlakeIDGen 初始化完成，workerId 是：{}", workerId);
    }

    @Override
    public synchronized long nextId() {
        long ts = nextTs();
        int sequence = seq.getAndIncrement();
        if (sequence >= sequenceMax) {
            seq.set(1);
            ts = nextTs(lastTs);
        }
        lastTs = ts;
        long diffTs = ts - beginTs;
        return (diffTs << timeBits) | (workerId << sequenceBits) | sequence;
    }

    private long currentTs() {
        return System.currentTimeMillis();
    }

    private long nextTs() {
        long ts = currentTs();
        while (ts < lastTs) {
            long offset = lastTs - ts;
            if (offset <= clockBack) {
                // 时钟回拨在可容忍范围内
                sleep(offset);
            } else {
                throw new SnowflakeClockBackException(String.valueOf(offset));
            }
            ts = currentTs();
        }
        return ts;
    }

    private long nextTs(long lastTs) {
        long ts = currentTs();
        while (ts <= lastTs) {
            sleep(lastTs - ts);
            ts = currentTs();
        }
        return ts;
    }

    private static void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            LOGGER.error("sleep interrupted", e);
        }
    }
}
