package com.ebay.dap.epic.tdq.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthToken {
    private String username;
    private String token;
    private LocalDateTime issuedTime;
    private LocalDateTime expiredTime;
}
