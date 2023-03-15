package com.ebay.dap.epic.tdq.data.bo.scorecard;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScorecardResult {

    private LocalDate dt;

    private List<CategoryResult> categoryResults;

    private String domainName;

    private Integer finalScore;

}
