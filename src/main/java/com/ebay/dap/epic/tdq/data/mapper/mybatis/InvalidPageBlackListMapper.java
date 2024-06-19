package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.InvalidPageBlackListEntity;

import java.util.List;

public interface InvalidPageBlackListMapper extends BaseMapper<InvalidPageBlackListEntity> {

    default List<Integer> listPageIdsInBlackList(){
            return this.selectList(Wrappers.emptyWrapper()).stream().map(InvalidPageBlackListEntity::getPageId).toList();
    }
}
