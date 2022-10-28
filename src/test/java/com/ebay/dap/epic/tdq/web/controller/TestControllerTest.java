package com.ebay.dap.epic.tdq.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(value = TestController.class)
class TestControllerTest extends AbstractApiBaseTest {

  // @Test
  void test_ok() throws Exception {
    mvc.perform(get("/api/test"))
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));
  }
}