package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.ProfilingUnusedPageInfo;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public interface ProfilingUnusedPageMapper extends BaseMapper<ProfilingUnusedPageInfo> {
    default List<ProfilingUnusedPageInfo> findAllByPageFamilyNameInAndDt(List<String> pageFamilyNameList, LocalDate localDate) {
        LambdaQueryWrapper<ProfilingUnusedPageInfo> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(pageFamilyNameList)){
            pageFamilyNameList = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(ProfilingUnusedPageInfo::getPageFamilyName, pageFamilyNameList);
        lambdaQueryWrapper.eq(ProfilingUnusedPageInfo::getDt, localDate);
        return selectList(lambdaQueryWrapper);
    }

    default List<ProfilingUnusedPageInfo> findAllByDtIn(List<LocalDate> singletonList) {
        LambdaQueryWrapper<ProfilingUnusedPageInfo> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(singletonList)){
            singletonList = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(ProfilingUnusedPageInfo::getDt, singletonList);
        return selectList(lambdaQueryWrapper);
    }

    default ProfilingUnusedPageInfo getOneByPageFamilyNameAndDt(String pageFamilyName, LocalDate localDate) {
        LambdaQueryWrapper<ProfilingUnusedPageInfo> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(ProfilingUnusedPageInfo::getPageFamilyName, pageFamilyName);
        lambdaQueryWrapper.eq(ProfilingUnusedPageInfo::getDt, localDate);
        return selectOne(lambdaQueryWrapper);
    }
}
