package com.ebay.dap.epic.tdq.data.bo.scorecard;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroovyScriptRule extends Rule {

    private String script;

}
