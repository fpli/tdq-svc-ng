package com.ebay.dap.epic.tdq.service.mmd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MMDAlert {

    String dtStr;

    BigDecimal rawValue;

    @JsonIgnore
    BigDecimal predValue;

    String predNRMSE;

    @JsonProperty("uBound")
    BigDecimal uBound;

    @JsonProperty("lBound")
    BigDecimal lBound;
    @JsonIgnore
    BigDecimal d;

    BigDecimal alertScore;

    String snrScore;

    Boolean isAnomaly;

    @JsonIgnore
    BigDecimal rawSNR;
}