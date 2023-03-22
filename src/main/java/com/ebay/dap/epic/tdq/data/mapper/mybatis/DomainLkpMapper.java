package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.scorecard.DomainLkpEntity;

import java.util.List;

public interface DomainLkpMapper extends BaseMapper<DomainLkpEntity> {

    default List<DomainLkpEntity> findAll() {
        return selectList(null);
    }

}
