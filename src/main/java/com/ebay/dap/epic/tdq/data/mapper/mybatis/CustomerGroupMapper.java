package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.CustomerGroupEntity;

import java.util.List;

public interface CustomerGroupMapper extends BaseMapper<CustomerGroupEntity> {
    default List<CustomerGroupEntity> findAll() {
        return selectList(null);
    }

    default CustomerGroupEntity getOneByApiKeyAndApiSecret(String apiKey, String apiSecret) {
        QueryWrapper<CustomerGroupEntity> queryWrapper = Wrappers.query();
        queryWrapper.eq("api_key", apiKey);
        queryWrapper.eq("api_secret", apiSecret);
        return selectOne(queryWrapper);
    }
}
