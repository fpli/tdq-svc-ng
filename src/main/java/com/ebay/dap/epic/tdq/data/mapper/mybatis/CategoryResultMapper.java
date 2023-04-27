package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.scorecard.CategoryResultEntity;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

public interface CategoryResultMapper extends BaseMapper<CategoryResultEntity> {

    @Select("select max(dt) from t_scorecard_category_result")
    LocalDate getMaxDt();

    @Select("select min(dt) from t_scorecard_category_result")
    LocalDate getMinDt();
}
