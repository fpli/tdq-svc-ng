package com.ebay.dap.epic.tdq.service;

import java.time.LocalDate;

public interface AnomalyDetector {

    void findAbnormalPages(LocalDate dt);
}
