package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.AlertSuppressionPageCfgEntity;
import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface AlertSuppressionPageCfgMapper extends BaseMapper<AlertSuppressionPageCfgEntity> {

    default List<Integer> listValidPageIds() {

        return listValidPageIds(null);
    }

    default List<Integer> listValidPageIds(LocalDate dt) {

        if (dt == null) {
            dt = LocalDate.now();
        }

        LambdaQueryWrapper<AlertSuppressionPageCfgEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();

        lambdaQueryWrapper.gt(AlertSuppressionPageCfgEntity::getSuppressUtil, dt);

        List<AlertSuppressionPageCfgEntity> entities = selectList(lambdaQueryWrapper);

        if (CollectionUtils.isNotEmpty(entities)) {
            return entities.stream()
                           .map(AlertSuppressionPageCfgEntity::getPageId)
                           .collect(Collectors.toSet())
                           .stream()
                           .toList();
        }

        return new ArrayList<>();
    }
}
