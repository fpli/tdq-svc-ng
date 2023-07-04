package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.vo.alert.EmailRecipient;
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
import java.util.List;

@Component
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    private static final String FROM_ADDRESS = "tdq-no-replay@ebay.com";
    private static final String FROM_NAME = "TDQ_AlertManager";

    @Override
    public void sendHtmlEmail(@NonNull String content, @NonNull List<String> to, List<String> cc, @NonNull String subject) throws Exception {
        log.info("Sending HTML Email to {} of subject {}", to, subject);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

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
    public void sendHtmlEmail(String title, @NonNull String templateName, Context context, EmailRecipient emailRecipient) throws Exception {
        this.sendHtmlEmail(title, templateName, context, emailRecipient.getTo(), emailRecipient.getCc());
    }

    @Override
    public void sendHtmlEmail(String title, @NonNull String templateName, Context context, List<String> to) throws Exception {
        this.sendHtmlEmail(title, templateName, context, to, null);
    }

    @Override
    public void sendHtmlEmail(String title, @NonNull String templateName, Context context, List<String> to, List<String> cc) throws Exception {
        log.info("Sending HTML Email using template {} with title {}", templateName, title);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        String content = templateEngine.process(templateName, context);

        helper.setFrom(FROM_ADDRESS, FROM_NAME);
        helper.setTo(to.toArray(new String[0]));
        helper.setSubject(title);
        helper.setText(content, true);

        if (CollectionUtils.isNotEmpty(cc)) {
            helper.setCc(cc.toArray(new String[0]));
        }

        mailSender.send(mimeMessage);
    }
}