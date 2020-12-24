package com.yesido.idgen.segment;

import com.yesido.idgen.core.IDGen;

public abstract class AbstractSegmentIDGen implements IDGen {

    @Override
    public long nextId() {
        throw new RuntimeException("nextId() can not be use");
    }

}
