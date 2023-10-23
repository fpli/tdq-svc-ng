package com.ebay.dap.epic.tdq.data.vo.alert;

import lombok.Data;

@Data
public class PageAlertItemVo {

    private Integer pageId;

    private String pageName;

    private String pageFmly;

    private Integer iFrame;

    private Long currentVal;

    private Long avgOfLast4W;

}
