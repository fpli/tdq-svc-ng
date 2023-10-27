package com.ebay.dap.epic.tdq.service;

import lombok.NonNull;
import org.thymeleaf.context.Context;

import java.util.List;

//TODO(yxiao6): this should be extracted to be a lib in the future
public interface EmailService {

    void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to) throws Exception;

    void sendHtmlEmail(@NonNull String templateName, Context context, String subject, List<String> to, List<String> cc) throws Exception;

    void sendEmail(@NonNull String emailTemplate, Context context, @NonNull String emailCfgName) throws Exception;

}