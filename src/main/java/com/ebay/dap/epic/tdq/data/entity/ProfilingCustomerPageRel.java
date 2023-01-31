package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("profiling_customer_page_rel")
public class ProfilingCustomerPageRel extends BaseEntity {

    @TableField("customer_id")
    private Long customerId;

    @TableField("page_id")
    private Integer pageId;
}