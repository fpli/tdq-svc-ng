package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.TagRecord;

import java.time.LocalDate;

public interface TagRecordMapper extends BaseMapper<TagRecord> {


    default long deleteAllByDate(LocalDate date) {
        LambdaQueryWrapper<TagRecord> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(TagRecord::getDt, date);
        return delete(lambdaQueryWrapper);
    }

    default long save(TagRecord tagRecord) {
        insert(tagRecord);
        return tagRecord.getId();
    }
}
