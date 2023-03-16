package com.ebay.dap.epic.tdq.data.repository;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebay.dap.epic.tdq.data.entity.scorecard.CategoryResultEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.CategoryResultMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoryResultRepositoryImpl extends ServiceImpl<CategoryResultMapper, CategoryResultEntity>
        implements CategoryResultRepository {

}
