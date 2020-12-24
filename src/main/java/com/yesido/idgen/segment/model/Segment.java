package com.yesido.idgen.segment.model;

import java.util.concurrent.atomic.AtomicLong;

public class Segment {

    private AtomicLong currentValue = new AtomicLong(0);
    private volatile long max;
    private volatile int step;
    private SegmentBuffer buffer;

    public Segment(SegmentBuffer buffer) {
        this.buffer = buffer;
    }

    public AtomicLong getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(AtomicLong currentValue) {
        this.currentValue = currentValue;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public SegmentBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(SegmentBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * 获取当前闲置的id数量
     * 
     * @author yesido
     * @date 2020年12月18日 下午3:33:27
     * @return
     */
    public long getIdle() {
        return this.max - this.currentValue.get();
    }

    @Override
    public String toString() {
        return "Segment [currentValue=" + currentValue + ", max=" + max + ", step=" + step + ", 剩余id数量=" + getIdle()
                + "]";
    }


}
