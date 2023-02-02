package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("profiling_tag_lkp")
public class TagLookUpInfo extends BaseEntity {
    String name;
    String description;

    String tagName;

    String dataType;

    String allowedValue;

    String dfltValue;

    String ownerEmail;

    String athrToolName;

    boolean lfcyclStateId;

    String actvInd;


    LocalDate firstSeenDt;


    LocalDate lastSeenDt;


    LocalDate creDate;

    String creUser;
}