package com.yesido.idgen.snowflake.exception;

public class SnowflakeClockBackException extends SnowflakeException {

    private static final long serialVersionUID = 1L;

    private String msg;

    public SnowflakeClockBackException(String msg) {
        this.msg = msg;
        setStatus(EXCEPTION_CLOCL_BACK);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
