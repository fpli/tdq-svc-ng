package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.TagUsageInfoEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface TagUsageInfoMapper extends BaseMapper<TagUsageInfoEntity> {

    @Select("select tag_name from (SELECT tag_name, SUM(access_count) cnt from profiling_tag_usage where dt = #{dt} group by tag_name order by cnt desc limit #{n}) t")
    Collection<String> listTagNames(@Param("n") int tagTopN, @Param("dt") LocalDate date);

    @Select({"<script>",
            "select * from profiling_tag_usage where dt in ",
            "<foreach item='item' index='index' collection='dates'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    List<TagUsageInfoEntity> findAllByDtIn(@Param("dates") List<LocalDate> singletonList);
    @Select("select * from profiling_tag_usage where tag_name = #{tagName} and dt between #{begin} and #{end} order by dt")
    List<TagUsageInfoEntity> findAllByTagNameAndDtBetweenOrderByDt(@Param("tagName") String tagName, @Param("begin") LocalDate begin, @Param("end") LocalDate end);
}
