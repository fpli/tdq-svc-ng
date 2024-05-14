package com.ebay.dap.epic.tdq.data.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CJSAdsAlertDTO {
    String groupName;
    double threshold;
    String dt;
    long cnt;

    List<CJSAdsAlertItemDTO> list = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CJSAdsAlertItemDTO {
        String adsType;
        String metricType;
        long  baseline;
        long value;
        double diff;
    }
}
