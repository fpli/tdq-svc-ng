package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("tag_record")
public class TagRecord extends BaseEntity {

    String tagName;

    String description;

    private LocalDate dt;


    @TableField("volume")
    double tagVolume;


    double eventVolume;

    @TableField("usage_volume")
    long accessTotal;

}