package com.ebay.dap.epic.tdq.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.UserEntity;
import com.ebay.dap.epic.tdq.data.enums.Role;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.UserMapper;
import com.ebay.dap.epic.tdq.security.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<UserEntity> queryWrapper = Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getUsername, username);
        UserEntity userEntity = userMapper.selectOne(queryWrapper);
        if (userEntity != null) {
            return User.fromUserEntity(userEntity);
        }
        return null;
    }

    @Override
    public User login(String username) {
        User user = this.getByUsername(username);
        UserEntity userEntity = null;
        if (user == null) {
            userEntity = User.createWithUserRole(username).toUserEntity();
            userMapper.insert(userEntity);
        } else {
            userEntity = user.toUserEntity();
            userEntity.setLastLoginTime(LocalDateTime.now());

            LambdaQueryWrapper<UserEntity> queryWrapper =
                    Wrappers.<UserEntity>lambdaQuery()
                            .eq(UserEntity::getUsername, username);
            userMapper.update(userEntity, queryWrapper);
        }
        return User.fromUserEntity(userEntity);
    }

    @Override
    public User createUser(String username, Role... roles) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        if (roles == null || roles.length == 0) {
            // if role is not set, then use default User role
            user.setRoles(Collections.singletonList(Role.USER));
        } else {
            user.setRoles(Arrays.stream(roles).toList());
        }
        user.setIsActive(true);
        user.setLastLoginTime(LocalDateTime.now());

        userMapper.insert(user);

        return User.fromUserEntity(user);
    }
}
