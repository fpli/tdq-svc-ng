package com.ebay.dap.epic.tdq.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("externalEmailService")
@Slf4j
public class ExternalEmailServiceImpl extends EmailServiceImpl {

    @Autowired
    public ExternalEmailServiceImpl(@Qualifier("externalMailSender") JavaMailSender mailSender) {
        super(mailSender);
    }

}