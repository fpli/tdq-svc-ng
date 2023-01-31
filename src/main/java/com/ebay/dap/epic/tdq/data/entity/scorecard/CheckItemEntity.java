package com.ebay.dap.epic.tdq.data.entity.scorecard;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.entity.AuditableEntity;
import lombok.Data;

@Data
@TableName("t_scorecard_check_item")
public class CheckItemEntity extends AuditableEntity {

    private String metricKey;

    private String category;

    private Integer executeOrder;

    private String script;

}
