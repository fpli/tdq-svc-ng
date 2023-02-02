package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("profiling_page_unused")
public class ProfilingUnusedPageInfo extends BaseEntity {

    @TableField("page_fmly")
    String pageFamilyName;
    @TableField("unused_cnt")
    Long unusedCount;
    LocalDate dt;
}