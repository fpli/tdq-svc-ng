package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.PageLookUpInfo;

import java.util.ArrayList;
import java.util.List;

public interface PageLookUpInfoMapper extends BaseMapper<PageLookUpInfo> {
    default List<PageLookUpInfo> findAllByPageIdIn(List<Integer> pageIds) {
        LambdaQueryWrapper<PageLookUpInfo> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (pageIds.isEmpty()){
            pageIds = new ArrayList<>(pageIds);
            pageIds.add(-1);
        }
        lambdaQueryWrapper.in(PageLookUpInfo::getPageId, pageIds);
        return selectList(lambdaQueryWrapper);
    }

    default List<PageLookUpInfo> findAll() {
        return selectList(null);
    }
}
