package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.ScorecardItemVO;

import java.time.LocalDate;
import java.util.List;

public interface ScorecardService {

    List<ScorecardItemVO> listScore(LocalDate date);

}
