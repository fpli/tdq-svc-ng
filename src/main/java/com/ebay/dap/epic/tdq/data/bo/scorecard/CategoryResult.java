package com.ebay.dap.epic.tdq.data.bo.scorecard;

import com.ebay.dap.epic.tdq.data.enums.Category;
import lombok.Data;

import java.util.List;

@Data
public class CategoryResult {

    private Category category;

    List<Rule> rules;

    private Integer subTotal;

}
