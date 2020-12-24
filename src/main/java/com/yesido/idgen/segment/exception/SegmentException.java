package com.yesido.idgen.segment.exception;

public abstract class SegmentException extends RuntimeException {
    protected static final int EXCEPTION_ID_IDCACHE_INIT_FALSE = -2; // 未初始化
    protected static final int EXCEPTION_ID_KEY_NOT_EXISTS = -3; // key不存在
    protected static final int EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL = -4; // SegmentBuffer中的两个Segment均未从DB中装载时的异常码
    private static final long serialVersionUID = 1L;
    private String key;
    private int status = -1;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
