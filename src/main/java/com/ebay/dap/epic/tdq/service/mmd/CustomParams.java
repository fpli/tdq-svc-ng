package com.ebay.dap.epic.tdq.service.mmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomParams {
    @JsonProperty("SHOW_BOUND_LEN")
    int showBoundLen;

    @JsonProperty("TH_ANOMALY_SCORE")
    BigDecimal thAnomalyScore;

    @JsonProperty("LINEAR_WIN")
    BigDecimal lineARWin;

    @JsonProperty("SEASONAL_PERIOD")
    BigDecimal seasonalPeriod;

    @JsonProperty("TREND_WIN")
    int trendWin;

    @JsonProperty("RSD_WIN")
    int rsdWin;

    @JsonProperty("CHECK_WIN")
    int checkWin;

    @JsonProperty("EXTRA_BOUNDARY_BUFFER")
    BigDecimal extraBoundaryBuffer;

    @JsonProperty("HIDE_NORMAL_DETAIL")
    Boolean hideNormalDetail;
}