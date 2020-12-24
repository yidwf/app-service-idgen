package com.yesido.idgen.segment.exception;

public class SegmentKeyNotExistException extends SegmentException {
    private static final long serialVersionUID = 1L;

    private String msg;

    public SegmentKeyNotExistException(String msg, String key) {
        this.msg = msg;
        setKey(key);
        setStatus(EXCEPTION_ID_KEY_NOT_EXISTS);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
