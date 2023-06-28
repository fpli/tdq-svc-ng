package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.ChartInfoEntity;

import java.util.List;

public interface ChartInfoMapper extends BaseMapper<ChartInfoEntity> {

    default List<ChartInfoEntity> findAll() {
        return selectList(null);
    }
}
