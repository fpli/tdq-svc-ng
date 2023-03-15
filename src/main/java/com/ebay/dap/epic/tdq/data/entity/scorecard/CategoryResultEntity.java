package com.ebay.dap.epic.tdq.data.entity.scorecard;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.entity.AuditableEntity;
import com.ebay.dap.epic.tdq.data.enums.Category;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("t_scorecard_category_result")
public class CategoryResultEntity extends AuditableEntity {

    private String domain;

    private Category category;

    private Integer subTotal;

    private Integer finalScore;

    private LocalDate dt;

}
