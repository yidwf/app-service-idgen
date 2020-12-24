package com.yesido.idgen.segment.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yesido.idgen.segment.mapper.LeafAllocMapper;
import com.yesido.idgen.segment.model.LeafAlloc;

/**
 * LeafAllocDao
 * 
 * @author yesido
 * @date 2020年12月24日 上午10:46:15
 */
@Repository
public class LeafAllocDao {

    @Autowired
    private LeafAllocMapper leafAllocMapper;

    @Transactional
    public LeafAlloc updateMaxIdAndGetLeafAlloc(String tag) {
        leafAllocMapper.updateMaxId(tag);
        return leafAllocMapper.get(tag);
    }

    public List<String> listAllBizTags() {
        return leafAllocMapper.listAllBizTags();
    }

    @Transactional
    public LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc alloc) {
        leafAllocMapper.updateMaxIdByCustomStep(alloc);
        return leafAllocMapper.get(alloc.getBiz_tag());
    }
}
