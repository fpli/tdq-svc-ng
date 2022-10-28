package com.ebay.dap.epic.tdq.security.model;

import com.ebay.dap.epic.tdq.data.entity.UserEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private String username;
  private String password;
  private List<Role> roles = new ArrayList<>();
  private LocalDateTime lastLoginDate;
  private boolean isActive = true;

  public static User fromUserEntity(UserEntity userEntity) {
    Preconditions.checkNotNull(userEntity);

    User user = new User();
    user.setUsername(userEntity.getUsername());
    user.setActive(userEntity.getIsActive());

    List<Role> roles = new ArrayList<>();
    for (String r : userEntity.getRoles().split(",")) {
      roles.add(Role.fromCode(Integer.parseInt(r)));
    }
    user.setRoles(roles);

    user.setLastLoginDate(userEntity.getLastLoginDate());

    return user;
  }

  public UserEntity toUserEntity() {
    UserEntity user = new UserEntity();

    user.setUsername(this.username);
    user.setIsActive(this.isActive);

    String roles = this.roles.stream().map(Role::getCode).collect(Collectors.joining(","));
    user.setRoles(roles);

    user.setLastLoginDate(this.lastLoginDate == null ? LocalDateTime.now() : this.lastLoginDate);

    return user;
  }


  public static User createWithUserRole(String username) {
    Preconditions.checkNotNull(username);

    User user = new User();
    user.setUsername(username);
    user.setActive(true);
    user.setRoles(Lists.newArrayList(Role.USER));
    user.setLastLoginDate(LocalDateTime.now());

    return user;
  }

  @Override
  public String toString() {
    return "User{" +
        "username=" + username +
        ", password=******" +
        ", roles=" + roles +
        ", lastLoginDate=" + lastLoginDate +
        ", isActive=" + isActive +
        "}";
  }
}
