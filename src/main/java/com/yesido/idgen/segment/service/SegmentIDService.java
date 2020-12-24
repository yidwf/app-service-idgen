package com.yesido.idgen.segment.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.yesido.idgen.core.IDGen;
import com.yesido.idgen.core.IDResult;
import com.yesido.idgen.segment.exception.SegmentException;

@Service
public class SegmentIDService {
    @Autowired
    @Qualifier("segmentIDGen")
    private IDGen idGen;

    @PostConstruct
    public void init() {
        idGen.init();
    }

    public IDResult nextId(String key) {
        try {
            long id = idGen.nextId(key);
            return IDResult.ok(id);
        } catch (SegmentException e) {
            return IDResult.error(e);
        } catch (Exception e) {
            return IDResult.error();
        }
    }
}
