package com.ebay.dap.epic.tdq.data.mapper.mystruct;

import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.vo.metric.MetricInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MetricMapper {


    MetricInfoEntity toEntity(MetricInfoVO vo);

}
