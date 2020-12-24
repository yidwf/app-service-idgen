package com.yesido.idgen.core;

import com.yesido.idgen.segment.exception.SegmentException;
import com.yesido.idgen.snowflake.exception.SnowflakeException;

/**
 * id对象
 * 
 * @author yesido
 * @date 2020年12月17日 上午11:44:08
 */
public class IDResult {
    protected static final int SUCCESS = 0; // 正常
    protected static final int UNKNOW = -1; // 未知异常
    private long id; // ID
    private int status; // 0=成功，-1未知异常，-2=未初始化，-3=key不存在，-4=segment not ready

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public IDResult(long id, int status) {
        this.id = id;
        this.status = status;
    }

    public static IDResult ok(long id) {
        return new IDResult(id, SUCCESS);
    }

    public static IDResult error() {
        return new IDResult(UNKNOW, UNKNOW);
    }

    public static IDResult newIDResult(long id, int status) {
        return new IDResult(id, status);
    }

    public static IDResult error(SegmentException e) {
        return new IDResult(e.getStatus(), e.getStatus());
    }

    public static IDResult error(SnowflakeException e) {
        return new IDResult(e.getStatus(), e.getStatus());
    }
}
