package com.ebay.dap.epic.tdq.service.impl;

import java.util.List;
import javax.mail.internet.MimeMessage;

import com.ebay.dap.epic.tdq.service.EmailService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendHtmlEmail(@NonNull String content, @NonNull List<String> to, List<String> cc, @NonNull String subject) throws Exception {
        log.info("Sending HTML Email to {} of subject {}", to, subject);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom("tdq-no-replay@ebay.com", "TDQ_AlertManager");
        helper.setTo(to.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(content, true);

        if (CollectionUtils.isNotEmpty(cc)) {
            helper.setCc(cc.toArray(new String[0]));
        }

        mailSender.send(mimeMessage);
    }
}