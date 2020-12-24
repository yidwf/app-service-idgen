package com.yesido.idgen.segment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yesido.idgen.segment.model.LeafAlloc;

@Mapper
public interface LeafAllocMapper {

    @Select("SELECT * FROM leaf_alloc")
    List<LeafAlloc> listAll();

    @Select("SELECT biz_tag FROM leaf_alloc")
    List<String> listAllBizTags();

    @Select("SELECT * FROM leaf_alloc WHERE biz_tag = #{biz_tag}")
    LeafAlloc get(@Param("biz_tag") String bizTag);

    @Update("UPDATE leaf_alloc SET max_id = max_id + step WHERE biz_tag = #{biz_tag}")
    int updateMaxId(@Param("biz_tag") String tag);

    @Update("UPDATE leaf_alloc SET max_id = max_id + #{step} WHERE biz_tag = #{biz_tag}")
    void updateMaxIdByCustomStep(LeafAlloc leafAlloc);
}
