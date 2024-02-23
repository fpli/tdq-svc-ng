package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("w_page_pool_lkp")
public class PagePoolLKP extends BaseEntity {

    @TableField("page_id")
    private Integer pageId;

    @TableField("environment")
    private String environment;

    @TableField("state")
    private String state;

    private long  traffic;

    @TableField("pool_name")
    private String poolName;

    @TableField("dt")
    private LocalDate dt;
}
