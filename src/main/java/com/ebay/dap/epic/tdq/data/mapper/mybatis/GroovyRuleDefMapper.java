package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.scorecard.GroovyRuleDefEntity;
import com.ebay.dap.epic.tdq.data.enums.Category;

import java.util.List;

public interface GroovyRuleDefMapper extends BaseMapper<GroovyRuleDefEntity> {

    default List<Category> listAllCategories(){
        QueryWrapper<GroovyRuleDefEntity> emptyWrapper = Wrappers.emptyWrapper();
        List<GroovyRuleDefEntity> groovyRuleDefEntityList = selectList(emptyWrapper);
        return groovyRuleDefEntityList.stream().map(GroovyRuleDefEntity::getCategory).distinct().toList();
    }
}
