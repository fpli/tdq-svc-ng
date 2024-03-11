package com.ebay.dap.epic.tdq.data.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidPageAlertDTO {
    String owner;
    String dt;
    String poolName;
    List<Integer> pageIds = new ArrayList<>();
}
