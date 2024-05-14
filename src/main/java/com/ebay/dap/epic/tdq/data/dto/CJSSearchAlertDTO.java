package com.ebay.dap.epic.tdq.data.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CJSSearchAlertDTO {
    String groupName;
    String begin;
    String end;
    double threshold;
    String dt;
    long cnt;
    List<CJSSearchAlertItemDTO> list;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CJSSearchAlertItemDTO {
        String metricKey;
        String kind;
        long   threshold;
        long   value;
        double diff;
    }
}

