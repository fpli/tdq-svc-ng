package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_alert_suppression_page_cfg")
public class AlertSuppressionPageCfgEntity extends AuditableEntity {

    private Integer pageId;

    private LocalDateTime suppressUtil;

}
