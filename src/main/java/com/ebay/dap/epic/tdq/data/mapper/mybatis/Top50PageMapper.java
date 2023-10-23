package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.Top50PageEntity;

import java.util.List;

public interface Top50PageMapper extends BaseMapper<Top50PageEntity> {

    default List<Top50PageEntity> findAll() {
        return selectList(null);
    }
}
