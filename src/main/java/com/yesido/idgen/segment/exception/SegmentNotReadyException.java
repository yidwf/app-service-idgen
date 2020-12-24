package com.yesido.idgen.segment.exception;

public class SegmentNotReadyException extends SegmentException {
    private static final long serialVersionUID = 1L;

    private String msg;

    public SegmentNotReadyException(String msg, String key) {
        this.msg = msg;
        setKey(key);
        setStatus(EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
