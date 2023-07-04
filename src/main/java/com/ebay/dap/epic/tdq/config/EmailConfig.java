package com.ebay.dap.epic.tdq.config;

import com.sun.mail.smtp.SMTPTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Session;
import java.util.Properties;

@Configuration
@Slf4j
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private Integer port;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
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
            log.error("Failed to send email", e);
        }
        javaMailSender.setSession(session);
        return javaMailSender;
    }
}