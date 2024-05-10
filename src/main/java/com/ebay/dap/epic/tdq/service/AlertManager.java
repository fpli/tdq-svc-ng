package com.ebay.dap.epic.tdq.service;


import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AlertManager {

  //TODO(yxiao6): add slack, pagerduty channel in the future
  void sendPageProfilingAlertEmail(LocalDate dt) throws Exception;

  void multipleUidAlert(LocalDateTime dateTime) throws Exception;

  void checkDailyData() throws Exception;

  void alertForEPTeamAndFamx(LocalDateTime localDateTime) throws Exception;

  void adsClickFraud(LocalDate dateTime) throws Exception;

  void cjsSearchMetricAbnormalDetection(LocalDate dateTime) throws Exception;
}