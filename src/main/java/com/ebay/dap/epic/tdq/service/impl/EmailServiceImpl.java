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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailConfigEntityMapper emailConfigMapper;

    private static final String FROM_ADDRESS = "tdq-no-replay@ebay.com";
    private static final String FROM_NAME = "TDQ_AlertManager";


    @Override
    public void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to) throws Exception {
        this.sendHtmlEmail(templateName, context, subject, to, null);
    }

    @Override
    public void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to, List<String> cc) throws Exception {
        log.info("Sending HTML Email using template {} with subject {}", templateName, subject);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        String content = templateEngine.process(templateName, context);

        helper.setFrom(FROM_ADDRESS, FROM_NAME);
        helper.setTo(to.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(content, true);

        if (CollectionUtils.isNotEmpty(cc)) {
            helper.setCc(cc.toArray(new String[0]));
        }

        mailSender.send(mimeMessage);
    }

    @Override
    public void sendEmail(@NonNull String emailTemplate, Context context, @NonNull String emailCfgName) throws Exception {

        LambdaQueryWrapper<EmailConfigEntity> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(EmailConfigEntity::getName, emailCfgName);
        EmailConfigEntity emailConfigEntity = emailConfigMapper.selectOne(lambdaQuery);

        if (emailConfigEntity == null) {
            throw new IllegalStateException("email config is not found");
        }

        List<String> to = Arrays.stream(emailConfigEntity.getRecipient().split(",")).toList();
        List<String> cc = null;
        if (emailConfigEntity.getCc() != null) {
            cc = Arrays.stream(emailConfigEntity.getCc().split(",")).toList();
        }
        this.sendHtmlEmail(emailTemplate, context, emailConfigEntity.getSubject(), to, cc);
    }
}