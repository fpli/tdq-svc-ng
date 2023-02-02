package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.NonBotPageCountEntity;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

public interface NonBotPageCountMapper extends BaseMapper<NonBotPageCountEntity> {

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
}
