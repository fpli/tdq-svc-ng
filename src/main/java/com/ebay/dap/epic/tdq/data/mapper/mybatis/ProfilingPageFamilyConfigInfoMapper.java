package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.ProfilingPageFamilyConfigInfo;

public interface ProfilingPageFamilyConfigInfoMapper extends BaseMapper<ProfilingPageFamilyConfigInfo> {
    default ProfilingPageFamilyConfigInfo getOneByName(String name) {
        LambdaQueryWrapper<ProfilingPageFamilyConfigInfo> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(ProfilingPageFamilyConfigInfo::getName, name);
        return selectOne(lambdaQueryWrapper);
    }

    default int save(ProfilingPageFamilyConfigInfo familyConfigInfo) {
        return insert(familyConfigInfo);
    }
}
