package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.scorecard.DomainWeightCfgEntity;

import java.util.List;

public interface DomainWeightCfgMapper extends BaseMapper<DomainWeightCfgEntity> {

    default List<DomainWeightCfgEntity> findAll() {
        return selectList(null);
    }

}
