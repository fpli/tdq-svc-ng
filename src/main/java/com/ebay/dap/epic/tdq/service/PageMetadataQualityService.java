package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.entity.UnregisterPageMetadataEntity;
import com.ebay.dap.epic.tdq.data.vo.BaseGeneralVO;

import java.time.LocalDate;

public interface PageMetadataQualityService {

    int dailyTask(LocalDate date);

    BaseGeneralVO<UnregisterPageMetadataEntity> listAllUnregisterPage(LocalDate date);

}
