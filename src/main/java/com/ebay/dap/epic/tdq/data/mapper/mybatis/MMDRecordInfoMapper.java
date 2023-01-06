package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.MMDRecordInfo;

public interface MMDRecordInfoMapper extends BaseMapper<MMDRecordInfo> {
    default long save(MMDRecordInfo mmdRecordInfo){
        insert(mmdRecordInfo);
        return mmdRecordInfo.getId();
    }
}
