package com.ebay.dap.epic.tdq.data.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebay.dap.epic.tdq.data.entity.AuthToken;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.AuthTokenMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthTokenRepositoryImpl extends ServiceImpl<AuthTokenMapper, AuthToken> implements AuthTokenRepository {
}
