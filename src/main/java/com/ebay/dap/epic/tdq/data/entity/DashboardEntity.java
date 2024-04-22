package com.ebay.dap.epic.tdq.data.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_tdq_dashboard")
public class DashboardEntity extends AuditableEntity {
    private String name;
    private String description;
    private String chartList;
}
