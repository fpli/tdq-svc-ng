package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.NonBotPageCountEntity;
import com.ebay.dap.epic.tdq.data.entity.PageLookUpInfo;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public interface NonBotPageCountMapper extends MPJBaseMapper<NonBotPageCountEntity> {

    default List<NonBotPageCountEntity> findAllByPageIdInAndDtIn(List<Integer> nonePageIds, List<String> dts) {
        LambdaQueryWrapper<NonBotPageCountEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(nonePageIds)) {
            nonePageIds = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(NonBotPageCountEntity::getPageId, nonePageIds);
        if (CollectionUtils.isEmpty(dts)) {
            dts = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(NonBotPageCountEntity::getDt, dts);
        return selectList(lambdaQueryWrapper);
    }


    default long countDistinctPageIds(List<Integer> pageIds, List<String> dts) {
        QueryWrapper<NonBotPageCountEntity> query = Wrappers.query();
        LambdaQueryWrapper<NonBotPageCountEntity> lambdaQueryWrapper = query.select("distinct page_id").lambda();
        if (CollectionUtils.isEmpty(pageIds)) {
            pageIds = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(NonBotPageCountEntity::getPageId, pageIds);
        if (CollectionUtils.isEmpty(dts)) {
            dts = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(NonBotPageCountEntity::getDt, dts);
        return selectCount(lambdaQueryWrapper);
    }


    default List<NonBotPageCountEntity> findAllByPageIdInAndDtGreaterThanEqualAndDtLessThanEqual(List<Integer> pageIds, String fromDt, String toDt) {
        LambdaQueryWrapper<NonBotPageCountEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(pageIds)) {
            pageIds = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(NonBotPageCountEntity::getPageId, pageIds);

        lambdaQueryWrapper.between(NonBotPageCountEntity::getDt, fromDt, toDt);
        return selectList(lambdaQueryWrapper);
    }

    default int deleteByDtLessThan(String dt) {
        LambdaQueryWrapper<NonBotPageCountEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.le(NonBotPageCountEntity::getDt, dt);
        return delete(queryWrapper);
    }

    default List<Integer> findPageIdsForMMD(LocalDate dt) {
        MPJLambdaWrapper<NonBotPageCountEntity> wrapper =
                JoinWrappers.lambda(NonBotPageCountEntity.class)
                            .select(NonBotPageCountEntity::getPageId)
                            .leftJoin(PageLookUpInfo.class, PageLookUpInfo::getPageId, NonBotPageCountEntity::getPageId)
                            .eq(NonBotPageCountEntity::getDt, dt.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                            .gt(NonBotPageCountEntity::getTotal, 10000000L)
                            .lt(PageLookUpInfo::getFirstSeenDt, dt.minusMonths(3));

        List<NonBotPageCountEntity> entities = selectJoinList(NonBotPageCountEntity.class, wrapper);
        return entities.stream().map(NonBotPageCountEntity::getPageId).distinct().toList();
    }

    @Select("select round(avg(t.total)) from profiling_page_count t where t.page_id = #{pageId} and t.dt between #{startDt} and #{endDt}")
    Long findAvgByPageIdAndBetweenDt(@Param("pageId") Integer pageId, @Param("startDt") String startDt, @Param("endDt") String endDt);

}
