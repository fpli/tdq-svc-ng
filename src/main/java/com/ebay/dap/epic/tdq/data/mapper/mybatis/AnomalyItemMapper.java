package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.AnomalyItemEntity;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public interface AnomalyItemMapper extends BaseMapper<AnomalyItemEntity> {

    default long deleteInBatch(String type, LocalDate localDate, String tag) {
        LambdaQueryWrapper<AnomalyItemEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AnomalyItemEntity::getType, type);
        lambdaQueryWrapper.eq(AnomalyItemEntity::getRefId, tag);
        lambdaQueryWrapper.eq(AnomalyItemEntity::getDt, localDate);
        return delete(lambdaQueryWrapper);
    }

    default long save(AnomalyItemEntity anomalyItemEntity) {
        insert(anomalyItemEntity);
        return anomalyItemEntity.getId();
    }

    default List<AnomalyItemEntity> findAllByTypeAndRefIdAndDtBetween(String type, String tagName, LocalDate begin, LocalDate dt) {
        LambdaQueryWrapper<AnomalyItemEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AnomalyItemEntity::getType, type);
        lambdaQueryWrapper.eq(AnomalyItemEntity::getRefId, tagName);
        lambdaQueryWrapper.between(AnomalyItemEntity::getDt, begin, dt);
        return selectList(lambdaQueryWrapper);
    }

    default void saveAll(List<AnomalyItemEntity> anomalyItems) {
        anomalyItems.forEach(this::insert);
    }

    default List<AnomalyItemEntity> findAllByTypeAndRefIdInAndDt(String type, List<String> refIds, LocalDate localDate) {
        LambdaQueryWrapper<AnomalyItemEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AnomalyItemEntity::getType, type);
        lambdaQueryWrapper.eq(AnomalyItemEntity::getDt, localDate);
        if (CollectionUtils.isEmpty(refIds)) {
            refIds = Collections.singletonList(null);
        }
        lambdaQueryWrapper.in(AnomalyItemEntity::getRefId, refIds);
        return selectList(lambdaQueryWrapper);
    }

    default List<AnomalyItemEntity> findAllAbnormalPagesOfDt(LocalDate dt){
        // SELECT t FROM AnomalyItemEntity t WHERE t.type='page' AND t.dt = ?1
        LambdaQueryWrapper<AnomalyItemEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AnomalyItemEntity::getType, "page");
        lambdaQueryWrapper.eq(AnomalyItemEntity::getDt, dt);
        return selectList(lambdaQueryWrapper);
    }
}
