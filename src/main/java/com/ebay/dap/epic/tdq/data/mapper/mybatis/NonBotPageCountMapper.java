package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.NonBotPageCountEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface NonBotPageCountMapper extends BaseMapper<NonBotPageCountEntity> {
    @Select({
            "<script>",
            "select * from profiling_page_count where page_id  in ",
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
    List<NonBotPageCountEntity> findAllByPageIdInAndDtIn(@Param("pageIds") List<Integer> nonePageIds, @Param("dts") List<String> dts);

    @Select({
            "<script>",
            "select count(distinct page_id) from profiling_page_count where page_id  in ",
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


    default List<NonBotPageCountEntity> findAllByPageIdInAndDtGreaterThanEqualAndDtLessThanEqual(List<Integer> pageIds, String fromDt, String toDt) {
        LambdaQueryWrapper<NonBotPageCountEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(NonBotPageCountEntity::getPageId, pageIds);
        lambdaQueryWrapper.ge(NonBotPageCountEntity::getDt, fromDt);
        lambdaQueryWrapper.le(NonBotPageCountEntity::getDt, toDt);
        return selectList(lambdaQueryWrapper);
    }

    default int deleteByDtLessThan(String dt) {
        QueryWrapper<NonBotPageCountEntity> queryWrapper = Wrappers.query();
        queryWrapper.le("dt", dt);
        return delete(queryWrapper);
    }
}
