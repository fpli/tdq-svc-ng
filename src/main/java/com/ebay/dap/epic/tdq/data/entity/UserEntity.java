package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.enums.Role;
import com.ebay.dap.epic.tdq.data.handler.RoleListTypeHandler;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

@Data
@TableName("t_user")
public class UserEntity extends AuditableEntity {

  private String username;

  private String firstName;

  private String lastName;

  private String email;

  @TableField(jdbcType = JdbcType.VARCHAR, typeHandler = RoleListTypeHandler.class)
  private List<Role> roles;

  private Boolean isActive;

  private LocalDateTime lastLoginTime;
}
