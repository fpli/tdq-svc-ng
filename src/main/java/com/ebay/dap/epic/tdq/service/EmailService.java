package com.ebay.dap.epic.tdq.service;

import lombok.NonNull;
import org.thymeleaf.context.Context;

import java.util.List;

//TODO(yxiao6): refactor me
public interface EmailService {

    void sendEmail(@NonNull String content, @NonNull String subject, @NonNull List<String> to, List<String> cc) throws Exception;

    void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to) throws Exception;

    void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to, List<String> cc) throws Exception;

    void sendHtmlEmail(@NonNull String templateName, Context context, @NonNull String emailCfgName) throws Exception;
}