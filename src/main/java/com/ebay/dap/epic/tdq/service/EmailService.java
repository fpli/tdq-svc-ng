package com.ebay.dap.epic.tdq.service;

import java.util.List;

import com.ebay.dap.epic.tdq.data.vo.alert.EmailRecipient;
import lombok.NonNull;
import org.thymeleaf.context.Context;

//TODO(yxiao6): this should be extracted to be a lib in the future
public interface EmailService {
    void sendHtmlEmail(@NonNull String content, @NonNull List<String> to, List<String> cc, @NonNull String subject) throws Exception;

    void sendHtmlEmail(String title, @NonNull String templateName, Context context, EmailRecipient emailRecipient) throws Exception;

    void sendHtmlEmail(String title, @NonNull String templateName, Context context, List<String> to) throws Exception;

    void sendHtmlEmail(String title, @NonNull String templateName, Context context, List<String> to, List<String> cc) throws Exception;

}