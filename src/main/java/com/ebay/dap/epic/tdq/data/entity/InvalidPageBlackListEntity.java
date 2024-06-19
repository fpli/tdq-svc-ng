package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_invalid_page_excluded_blacklist")
public class InvalidPageBlackListEntity extends AuditableEntity {
    @TableField("page_id")
    private Integer pageId;
}
