package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("profiling_page_count_bot")
public class BotPageCountEntity extends BaseEntity {

    @TableField("page_id")
    private Integer pageId;

    @TableField("total")
    private Long total;

    @TableField("dt")
    private String dt;
}