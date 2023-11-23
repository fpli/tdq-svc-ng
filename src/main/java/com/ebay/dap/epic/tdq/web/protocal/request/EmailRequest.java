package com.ebay.dap.epic.tdq.web.protocal.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class EmailRequest {

    @NotBlank
    private String content;

    @NotBlank
    private String subject;

    @NotEmpty
    private List<String> to;

    private List<String> cc;
}
