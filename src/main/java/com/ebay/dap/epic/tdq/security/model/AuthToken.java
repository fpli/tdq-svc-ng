package com.ebay.dap.epic.tdq.security.model;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
