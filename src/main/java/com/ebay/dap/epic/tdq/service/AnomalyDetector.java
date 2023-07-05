package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.service.mmd.MMDException;

import java.time.LocalDate;

public interface AnomalyDetector {

    // using MMD to detect abnormal pages based on traffic and save alerts into db
    void findAbnormalPages(LocalDate dt) throws MMDException;
}
