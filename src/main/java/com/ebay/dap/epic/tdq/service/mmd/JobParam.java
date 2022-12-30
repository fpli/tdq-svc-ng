package com.ebay.dap.epic.tdq.service.mmd;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobParam {
    String id;
    List<Series> series;
    String label;
}