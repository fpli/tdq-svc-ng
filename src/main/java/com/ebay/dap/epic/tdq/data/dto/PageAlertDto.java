package com.ebay.dap.epic.tdq.data.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageAlertDto<T> {
    private String groupName;
    private List<T> pages;
    private List<T> list;
    private String dt;
    private long cnt;
}