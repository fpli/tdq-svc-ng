package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.BotPageCountEntity;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

public interface BotPageCountMapper extends BaseMapper<BotPageCountEntity> {

    default List<BotPageCountEntity> findAllByPageIdInAndDtIn(List<Integer> nonePageIds, List<String> dts) {
        LambdaQueryWrapper<BotPageCountEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(nonePageIds)) {
            nonePageIds = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(BotPageCountEntity::getPageId, nonePageIds);
        if (CollectionUtils.isEmpty(dts)) {
            dts = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(BotPageCountEntity::getDt, dts);
        return selectList(lambdaQueryWrapper);
    }

    default long countDistinctPageIds(List<Integer> pageIds, List<String> dts) {
        QueryWrapper<BotPageCountEntity> query = Wrappers.query();
        LambdaQueryWrapper<BotPageCountEntity> lambdaQueryWrapper = query.select("distinct page_id").lambda();
        if (CollectionUtils.isEmpty(pageIds)) {
            pageIds = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(BotPageCountEntity::getPageId, pageIds);
        if (CollectionUtils.isEmpty(dts)) {
            dts = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(BotPageCountEntity::getDt, dts);
        return selectCount(lambdaQueryWrapper);
    }

    default List<BotPageCountEntity> findAllByPageIdInAndDtGreaterThanEqualAndDtLessThanEqual(List<Integer> pageIds, String fromDt, String toDt) {
        LambdaQueryWrapper<BotPageCountEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(pageIds)) {
            pageIds = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(BotPageCountEntity::getPageId, pageIds);
        lambdaQueryWrapper.between(BotPageCountEntity::getDt, fromDt, toDt);
        return selectList(lambdaQueryWrapper);
    }

    default int deleteByDtLessThan(String dt) {
        LambdaQueryWrapper<BotPageCountEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.le(BotPageCountEntity::getDt, dt);
        return delete(lambdaQueryWrapper);
    }
}
