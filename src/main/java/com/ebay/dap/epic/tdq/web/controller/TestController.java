package com.ebay.dap.epic.tdq.web.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Hidden
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
