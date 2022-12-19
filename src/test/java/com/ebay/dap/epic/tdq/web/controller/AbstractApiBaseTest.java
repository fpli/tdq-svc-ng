package com.ebay.dap.epic.tdq.web.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
public abstract class AbstractApiBaseTest {

  @Autowired
  protected MockMvc mvc;

  protected String getJsonStr(String jsonFileName) throws IOException {
    String filePath = "src/test/resources/test-data/" + jsonFileName;
    FileInputStream fis = new FileInputStream(filePath);
    return IOUtils.toString(fis, StandardCharsets.UTF_8);
  }

}
