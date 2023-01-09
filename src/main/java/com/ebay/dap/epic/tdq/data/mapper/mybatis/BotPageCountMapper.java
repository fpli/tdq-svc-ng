package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.BotPageCountEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BotPageCountMapper extends BaseMapper<BotPageCountEntity> {
    @Select({
            "<script>",
            "select * from profiling_page_count_bot where page_id  in ",
            "<foreach item='item' index='index' collection='pageIds'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            " and dt in ",
            "<foreach item='item' index='index' collection='dts'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<BotPageCountEntity> findAllByPageIdInAndDtIn(@Param("pageIds") List<Integer> nonePageIds, @Param("dts") List<String> dts);

    @Select({
            "<script>",
            "select count(distinct page_id) from profiling_page_count_bot where page_id in ",
            "<foreach item='item' index='index' collection='pageIds'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            " and dt in ",
            "<foreach item='item' index='index' collection='dts'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    long countDistinctPageIds(@Param("pageIds") List<Integer> pageIds, @Param("dts") List<String> dts);

    @Select({
            "<script>",
            "select * from profiling_page_count_bot where page_id  in ",
            "<foreach item='item' index='index' collection='pageIds'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            " and dt between #{fromDt} and #{toDt}",
            "</script>"
    })
    List<BotPageCountEntity> findAllByPageIdInAndDtGreaterThanEqualAndDtLessThanEqual(@Param("pageIds") List<Integer> pageIds, @Param("fromDt") String fromDt, @Param("toDt") String toDt);

    @Delete("delete from profiling_page_count_bot where dt < #{dt}")
    long deleteByDtLessThan(@Param("dt") String dt);
}
