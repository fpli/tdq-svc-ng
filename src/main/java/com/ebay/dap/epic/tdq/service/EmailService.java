package com.ebay.dap.epic.tdq.service;

import java.util.List;
import lombok.NonNull;

//TODO(yxiao6): this should be extracted to be a lib in the future
public interface EmailService {
    void sendHtmlEmail(@NonNull String content, @NonNull List<String> to, List<String> cc, @NonNull String subject) throws Exception;
}