package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.ProfilingCustomerPageRel;

import java.util.Collection;
import java.util.List;

public interface ProfilingCustomerPageRelMapper extends BaseMapper<ProfilingCustomerPageRel> {
    default List<ProfilingCustomerPageRel> findAllByCustomerId(Long customerId) {
        LambdaQueryWrapper<ProfilingCustomerPageRel> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(ProfilingCustomerPageRel::getCustomerId, customerId);
        return selectList(lambdaQueryWrapper);
    }

    default int deleteAll(List<ProfilingCustomerPageRel> profilingPageGroupList) {
        return deleteBatchIds(profilingPageGroupList.stream().map(ProfilingCustomerPageRel::getId).toList());
    }

    default Collection<ProfilingCustomerPageRel> saveAll(List<ProfilingCustomerPageRel> list) {
        list.forEach(this::insert);
        return list;
    }
}
