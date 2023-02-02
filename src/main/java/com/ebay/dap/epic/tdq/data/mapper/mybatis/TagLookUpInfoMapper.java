package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.TagLookUpInfo;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

public interface TagLookUpInfoMapper extends BaseMapper<TagLookUpInfo> {


    default List<TagLookUpInfo> findAllByTagName(String tagName) {
        LambdaQueryWrapper<TagLookUpInfo> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(TagLookUpInfo::getName, tagName);
        return selectList(lambdaQueryWrapper);
    }

    default List<TagLookUpInfo> findSomeByTagName(List<String> tagNames) {
        LambdaQueryWrapper<TagLookUpInfo> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isEmpty(tagNames)) {
            tagNames = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(TagLookUpInfo::getName, tagNames);
        return selectList(lambdaQueryWrapper);
    }


    default List<String> findAllTagNames() {
        return selectList(null).stream().map(TagLookUpInfo::getName).distinct().toList();
    }
}
