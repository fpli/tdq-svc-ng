package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.PageUsageEntity;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public interface PageUsageMapper extends BaseMapper<PageUsageEntity> {
    default List<PageUsageEntity> findAllByPageIdInAndDtIn(List<Integer> pageIds, List<LocalDate> dts) {
        LambdaQueryWrapper<PageUsageEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(pageIds)){
            pageIds = Collections.singletonList(null);
        }
        if (CollectionUtils.isEmpty(dts)){
            dts = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(PageUsageEntity::getPageId, pageIds);
        lambdaQueryWrapper.in(PageUsageEntity::getDt, dts);
        return selectList(lambdaQueryWrapper);
    }

    default List<PageUsageEntity> findAllByPageIdInAndDtBetween(List<Integer> pageIds, LocalDate startDt, LocalDate endDt) {
        LambdaQueryWrapper<PageUsageEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(pageIds)){
            pageIds = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(PageUsageEntity::getPageId, pageIds);
        lambdaQueryWrapper.ge(PageUsageEntity::getDt, startDt);
        lambdaQueryWrapper.le(PageUsageEntity::getDt, endDt);
        return selectList(lambdaQueryWrapper);
    }
}
