package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.service.AlertManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Slf4j
public class AlertManagerImpl implements AlertManager {

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendPageProfilingAlertEmail(LocalDate dt) throws Exception {

    }

    @Override
    public void multipleUidAlert(LocalDateTime dateTime) throws Exception {

    }

    @Override
    public void checkDailyData() throws Exception {

    }

    @Override
    public void alertForEPTeamAndFamx(LocalDateTime localDateTime) throws Exception {

    }
}
