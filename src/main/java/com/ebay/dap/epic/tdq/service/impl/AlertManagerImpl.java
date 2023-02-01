package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.service.AlertManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Log4j2
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
