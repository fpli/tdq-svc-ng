package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.ProfilingPageActivityStats;

import java.time.LocalDate;
import java.util.List;

public interface ProfilingPageActivityStatsMapper extends BaseMapper<ProfilingPageActivityStats> {
    default List<ProfilingPageActivityStats> findAllByDt(LocalDate localDate) {
        LambdaQueryWrapper<ProfilingPageActivityStats> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(ProfilingPageActivityStats::getDt, localDate);
        return selectList(lambdaQueryWrapper);
    }
}
