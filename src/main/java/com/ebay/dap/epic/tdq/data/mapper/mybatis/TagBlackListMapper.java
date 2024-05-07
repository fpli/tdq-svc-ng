package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.TagBlackListEntity;
import java.util.List;
import java.util.stream.Collectors;

public interface TagBlackListMapper extends BaseMapper<TagBlackListEntity> {

    default List<String> getAllTagInBlackList(){
            return this.selectList(null).stream().map(TagBlackListEntity::getTagName).collect(Collectors.toList());
    }
}
