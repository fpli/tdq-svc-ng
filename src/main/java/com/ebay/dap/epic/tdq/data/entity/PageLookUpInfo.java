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
@TableName("profiling_page_lkp")
public class PageLookUpInfo extends BaseEntity {

    @TableField("page_id")
    private Integer pageId;

    @TableField("page_name")
    String pageName;

    @TableField("page_desc")
    String pageDesc;

    @TableField("iframe")
    int iframe;

    @TableField("owner")
    String owner;

    @TableField("page_fmly")
    String pageFamily;

    @TableField("create_dt")
    private LocalDate dt;

    @TableField("page_first_seen_dt")
    private LocalDate firstSeenDt;

}