package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.service.EmailService;
import com.ebay.dap.epic.tdq.web.protocal.request.EmailRequest;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@Hidden
public class TestController {

    @Autowired
    private EmailService internalEmailService;

    @Autowired
    @Qualifier("externalEmailService")
    private EmailService externalEmailService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/test/email/internal")
    public ResponseEntity<String> testEmailInternal(@RequestBody @Valid EmailRequest emailRequest) throws Exception {
        internalEmailService.sendEmail(emailRequest.getContent(), emailRequest.getSubject(),
                emailRequest.getTo(), emailRequest.getCc());

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/test/email/external")
    public ResponseEntity<String> testEmailExternal(@RequestBody @Valid EmailRequest emailRequest) throws Exception {
        externalEmailService.sendEmail(emailRequest.getContent(), emailRequest.getSubject(),
                emailRequest.getTo(), emailRequest.getCc());

        return ResponseEntity.ok("OK");
    }

}
