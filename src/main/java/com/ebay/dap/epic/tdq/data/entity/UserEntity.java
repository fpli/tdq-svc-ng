package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("t_user")
public class UserEntity {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String username;

  private String roles;

  private Boolean isActive;

  private LocalDateTime lastLoginDate;
}
