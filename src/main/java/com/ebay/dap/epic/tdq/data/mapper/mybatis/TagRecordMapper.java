package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.TagRecord;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDate;

public interface TagRecordMapper extends BaseMapper<TagRecord> {

    @Delete("delete from tag_record where dt = #{date}")
    long deleteAllByDate(LocalDate date);

    default long save(TagRecord tagRecord) {
        insert(tagRecord);
        return tagRecord.getId();
    }
}
