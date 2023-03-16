package com.ebay.dap.epic.tdq.data.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebay.dap.epic.tdq.data.entity.scorecard.RuleResultEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.RuleResultMapper;
import org.springframework.stereotype.Component;

@Component
public class RuleResultRepositoryImpl extends ServiceImpl<RuleResultMapper, RuleResultEntity>
        implements RuleResultRepository {

}
