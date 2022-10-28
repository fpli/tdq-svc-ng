package com.ebay.dap.epic.tdq.data.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebay.dap.epic.tdq.data.entity.CustomTopicConfig;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.CustomTopicConfigMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomTopicConfigRepositoryImpl
    extends ServiceImpl<CustomTopicConfigMapper, CustomTopicConfig>
    implements CustomTopicConfigRepository {
}
