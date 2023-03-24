package com.ebay.dap.epic.tdq.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageAlertItemDto {
    private Integer pageId;
    private String pageName;
    private String pageFmly;
    private Integer iFrame;
    private Long avgLast7D;
    private Long volume;
}