package com.ebay.dap.epic.tdq.data.vo.email;

import lombok.Data;

import java.util.List;

@Data
public class EmailRecipient {

    private List<String> to;

    private List<String> cc;

    private List<String> bcc;

}
