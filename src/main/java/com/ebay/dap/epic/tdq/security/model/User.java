package com.ebay.dap.epic.tdq.security.model;

import com.ebay.dap.epic.tdq.data.entity.UserEntity;
import com.ebay.dap.epic.tdq.data.enums.Role;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    private String username;
    private String password;
    private List<Role> roles = new ArrayList<>();
    private LocalDateTime lastLoginTime;
    private boolean isActive = true;

    public static User fromUserEntity(UserEntity userEntity) {
        Preconditions.checkNotNull(userEntity);

        User user = new User();
        user.setUsername(userEntity.getUsername());
        user.setActive(userEntity.getIsActive());
        user.setRoles(userEntity.getRoles());
        user.setLastLoginTime(userEntity.getLastLoginTime());

        return user;
    }

    public UserEntity toUserEntity() {
        UserEntity user = new UserEntity();

        user.setUsername(this.username);
        user.setIsActive(this.isActive);

        // String roles = this.roles.stream().map(Role::getCode).collect(Collectors.joining(","));
        user.setRoles(this.roles);

        user.setLastLoginTime(this.lastLoginTime == null ? LocalDateTime.now() : this.lastLoginTime);

        return user;
    }


    public static User createWithUserRole(String username) {
        Preconditions.checkNotNull(username);

        User user = new User();
        user.setUsername(username);
        user.setActive(true);
        user.setRoles(Lists.newArrayList(Role.USER));
        user.setLastLoginTime(LocalDateTime.now());

        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                "username=" + username +
                ", password=******" +
                ", roles=" + roles +
                ", lastLoginTime=" + lastLoginTime +
                ", isActive=" + isActive +
                "}";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
