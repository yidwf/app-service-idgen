package com.yesido.idgen.snowflake;

import com.yesido.idgen.core.IDGen;

public abstract class AbstractSnowFlakeIDGen implements IDGen {

    @Override
    public long nextId(String key) {
        return nextId();
    }

}
