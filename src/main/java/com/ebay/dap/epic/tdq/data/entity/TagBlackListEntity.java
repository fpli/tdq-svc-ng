package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_tag_blacklist")
public class TagBlackListEntity extends AuditableEntity {
    private String tagName;
}
