package com.ebay.dap.epic.tdq.data.vo.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpAndFamxAlertVo<T> {
    private String groupName;
    private List<T> pages;
    private List<T> list;
    private String dt;
    private long cnt;
}
