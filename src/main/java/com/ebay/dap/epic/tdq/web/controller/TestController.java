package com.ebay.dap.epic.tdq.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api")
@ApiIgnore
public class TestController {

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/test_auth")
  public ResponseEntity<String> testAuth() {
    return ResponseEntity.ok("OK");
  }
}
