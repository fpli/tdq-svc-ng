package com.ebay.dap.epic.tdq.service.scorecard;

import java.time.LocalDate;

public interface ExecutionEngine {

    /***
     * Process the selected date's scorecard and save the results to database
     *
     * @param dt
     */
    void process(LocalDate dt);

}
