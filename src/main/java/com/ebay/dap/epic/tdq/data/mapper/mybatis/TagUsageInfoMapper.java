package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.TagUsageInfoEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface TagUsageInfoMapper extends BaseMapper<TagUsageInfoEntity> {

    @Select("select tag_name from (SELECT tag_name, SUM(access_count) cnt from profiling_tag_usage where dt = #{dt} group by tag_name order by cnt desc limit #{n}) t")
    Collection<String> listTagNames(@Param("n") int tagTopN, @Param("dt") LocalDate date);

    default List<TagUsageInfoEntity> findAllByDtIn(List<LocalDate> singletonList) {
        LambdaQueryWrapper<TagUsageInfoEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(singletonList)) {
            singletonList = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(TagUsageInfoEntity::getDt, singletonList);
        return selectList(lambdaQueryWrapper);
    }

    //@Select("select * from profiling_tag_usage where tag_name = #{tagName} and dt between #{begin} and #{end} order by dt")
    default List<TagUsageInfoEntity> findAllByTagNameAndDtBetweenOrderByDt(String tagName, LocalDate begin, LocalDate end) {
        LambdaQueryWrapper<TagUsageInfoEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(TagUsageInfoEntity::getTagName, tagName);
        lambdaQueryWrapper.between(TagUsageInfoEntity::getDt, begin, end);
        lambdaQueryWrapper.orderByAsc(TagUsageInfoEntity::getDt);
        return selectList(lambdaQueryWrapper);
    }
}
