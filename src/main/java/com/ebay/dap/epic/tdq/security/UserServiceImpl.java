package com.ebay.dap.epic.tdq.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.UserEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.UserMapper;
import com.ebay.dap.epic.tdq.security.model.Role;
import com.ebay.dap.epic.tdq.security.model.User;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;

  @Autowired
  public UserServiceImpl(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @Override
  public User getByUsername(String username) {
    LambdaQueryWrapper<UserEntity> queryWrapper =
        Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getUsername, username);
    UserEntity userEntity = userMapper.selectOne(queryWrapper);
    if (userEntity != null) {
      return User.fromUserEntity(userEntity);
    }
    return null;
  }

  @Override
  public User updateUserLastLoginDate(String username) {
    User user = this.getByUsername(username);
    UserEntity userEntity = null;
    if (user == null) {
      userEntity = User.createWithUserRole(username).toUserEntity();
      userMapper.insert(userEntity);
    } else {
      userEntity = user.toUserEntity();
      userEntity.setLastLoginDate(LocalDateTime.now());

      LambdaQueryWrapper<UserEntity> queryWrapper =
          Wrappers.<UserEntity>lambdaQuery()
                  .eq(UserEntity::getUsername, username);
      userMapper.update(userEntity, queryWrapper);
    }
    return User.fromUserEntity(userEntity);
  }

  @Override
  public User setUserRoles(List<Role> roles) {
    // TODO(yxiao6): to be implemented
    return null;
  }
}
