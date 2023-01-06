package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.TagLookUpInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TagLookUpInfoMapper extends BaseMapper<TagLookUpInfo> {

    @Select("select * from profiling_tag_lkp where tag_name = #{tagName}")
    List<TagLookUpInfo> findAllByTagName(@Param("tagName") String tagName);

    @Select({"<script>",
            "select * from profiling_tag_lkp where tag_name in ",
            "<foreach item='item' index='index' collection='tagNames'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    List<TagLookUpInfo> findSomeByTagName(@Param("tagNames") List<String> tagNames);

    @Select("select distinct tag_name from profiling_tag_lkp")
    List<String> findAllTagNames();
}
