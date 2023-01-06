package com.ebay.dap.epic.tdq.service.mmd;

import lombok.Data;

import java.util.List;

@Data
public class MMDRequest {

    private GlobalConfig globalConf;
    private List<JobParam> jobs;

    @Override
    public MMDRequest clone() {
        MMDRequest mmdRequest = new MMDRequest();
        mmdRequest.globalConf = globalConf;
        mmdRequest.jobs = null;
        return mmdRequest;
    }
}