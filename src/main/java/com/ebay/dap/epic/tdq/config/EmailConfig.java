package com.ebay.dap.epic.tdq.config;

import com.sun.mail.smtp.SMTPTransport;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.mail.DefaultAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Session;
import java.util.Properties;

@Configuration
@Log4j2
public class EmailConfig {

    @Bean("javaMailSender")
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("mx.vip.lvs.ebay.com");
        javaMailSender.setPort(25);
        Properties javaMailProperties = javaMailSender.getJavaMailProperties();
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        javaMailProperties.putAll(props);
        Session session = Session.getInstance(javaMailProperties, new DefaultAuthenticator("", ""));
        session.setDebug(false);
        try {
            SMTPTransport smtpTransport = (SMTPTransport) session.getTransport("smtp");
            smtpTransport.setReportSuccess(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e);
        }
        javaMailSender.setSession(session);
        return javaMailSender;
    }
}