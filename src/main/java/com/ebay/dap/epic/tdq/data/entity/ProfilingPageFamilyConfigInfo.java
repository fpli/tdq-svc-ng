package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("profiling_page_family")
public class ProfilingPageFamilyConfigInfo extends BaseEntity {

    String name;

    @TableField("page_family_config")
    String pageFamilyConfig;
}