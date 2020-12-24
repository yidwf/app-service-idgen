package com.yesido.idgen.snowflake.exception;

public abstract class SnowflakeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    protected static final int EXCEPTION_CLOCL_BACK = -5; // 时钟回拨
    private int status = -1;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
