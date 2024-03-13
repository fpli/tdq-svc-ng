package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.EmailConfigEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.EmailConfigEntityMapper;
import com.ebay.dap.epic.tdq.service.EmailService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

@Component
@Primary
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailConfigEntityMapper emailConfigMapper;

    private static final String FROM_ADDRESS = "tdq-no-replay@ebay.com";
    private static final String FROM_NAME = "TDQ_AlertManager";

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Override
    public void sendEmail(@NonNull String content, @NonNull String subject, @NonNull List<String> to, List<String> cc) throws Exception {
        sendEmail(content, subject, to, null, cc);
    }

    @Override
    public void sendEmail(@NonNull String content, @NonNull String subject, List<String> to, List<String> bcc, List<String> cc) throws Exception {
        log.info("Sending email with subject: {}, to: {}, bcc: {}, cc: {}", subject, to, bcc, cc);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        helper.setFrom(FROM_ADDRESS, FROM_NAME);
        helper.setSubject(subject);
        helper.setText(content, true);

        boolean flag = false;

        if (CollectionUtils.isNotEmpty(to)) {
            List<String> list = to.stream().map(String::strip).filter(item -> !item.isBlank()).toList();
            if (!list.isEmpty()){
                helper.setTo(list.toArray(new String[0]));
                flag = true;
            }
        }

        if (CollectionUtils.isNotEmpty(bcc)) {
            List<String> list = bcc.stream().map(String::strip).filter(item -> !item.isBlank()).toList();
            if (!list.isEmpty()){
                helper.setBcc(list.toArray(new String[0]));
                flag = true;
            }
        }

        if (CollectionUtils.isNotEmpty(cc)) {
            List<String> ccList = cc.stream().map(String::strip).filter(item -> !item.isBlank()).toList();
            if (!ccList.isEmpty()) {
                helper.setCc(ccList.toArray(new String[0]));
                flag = true;
            }
        }

        if (flag) {
            mailSender.send(mimeMessage);
        }
    }

    @Override
    public void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to) throws Exception {
        this.sendHtmlEmail(templateName, context, subject, to, null);
    }

    @Override
    public void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to, List<String> cc) throws Exception {
        log.info("Processing html email using template: {}, with subject: {}", templateName, subject);
        String content = templateEngine.process(templateName, context);
        sendEmail(content, subject, to, cc);
    }

    @Override
    public void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to, List<String> bcc, List<String> cc) throws Exception {
        log.info("Processing html email using template: {}, with subject: {}", templateName, subject);
        String content = templateEngine.process(templateName, context);
        sendEmail(content, subject, to, bcc, cc);
    }

    @Override
    public void sendHtmlEmail(@NonNull String templateName, Context context, @NonNull String emailCfgName) throws Exception {

        LambdaQueryWrapper<EmailConfigEntity> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(EmailConfigEntity::getName, emailCfgName);
        EmailConfigEntity emailConfigEntity = emailConfigMapper.selectOne(lambdaQuery);

        if (emailConfigEntity == null) {
            throw new IllegalStateException("email config is not found");
        }

        List<String> to = Arrays.stream(emailConfigEntity.getRecipient().split(",")).toList();
        List<String> cc = null;
        if (emailConfigEntity.getCc() != null && !emailConfigEntity.getCc().isBlank()) {
            cc = Arrays.stream(emailConfigEntity.getCc().split(","))
                       .map(String::strip)
                       .filter(item -> !item.isBlank())
                       .toList();
        }
        this.sendHtmlEmail(templateName, context, emailConfigEntity.getSubject(), to, cc);
    }
}