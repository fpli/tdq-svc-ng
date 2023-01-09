package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("customer_group")
public class CustomerGroupEntity extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("email_dl")
    private String emailDl;

    @TableField("api_key")
    private String apiKey;

    @TableField("api_secret")
    private String apiSecret;

}