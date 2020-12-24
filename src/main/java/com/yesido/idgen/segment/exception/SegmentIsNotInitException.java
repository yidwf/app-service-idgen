package com.yesido.idgen.segment.exception;

public class SegmentIsNotInitException extends SegmentException {
    private static final long serialVersionUID = 1L;

    private String msg;

    public SegmentIsNotInitException(String msg, String key) {
        this.msg = msg;
        setKey(key);
        setStatus(EXCEPTION_ID_IDCACHE_INIT_FALSE);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
