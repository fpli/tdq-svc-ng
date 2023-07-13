package com.ebay.dap.epic.tdq.web.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api/task")
@Hidden
public class ScheduledTaskController {


    @Autowired
    private ApplicationContext context;

    @PostMapping("/run")
    public String runTask(@RequestBody Map<String, String> body) throws Exception {
        String task = body.get("task");
        log.info("Trigger scheduled task: {} via API", task);

        Object obj = context.getBean(task);
        MethodInvokingBean bean = new MethodInvokingBean();
        bean.setTargetObject(obj);
        bean.setTargetMethod("run");

        bean.prepare();
        bean.invoke();
        return "OK";
    }

}
