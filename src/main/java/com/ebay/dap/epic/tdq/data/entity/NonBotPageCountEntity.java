package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("profiling_page_count")
public class NonBotPageCountEntity extends BaseEntity {

    @TableField("page_id")
    private Integer pageId;

    @TableField("total")
    private Long total;

    @TableField("dt")
    private String dt;
}