package com.ebay.dap.epic.tdq.security.keystone.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class KeystoneToken {

  @JsonProperty("issued_at")
  private LocalDateTime issuedAt;
  private ZonedDateTime expires;
  @JsonProperty("id")
  private String token;
}
