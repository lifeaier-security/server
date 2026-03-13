package com.lifeaier.base.comm.token.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.lifeaier.base.comm.token.entity.TokenRefreshEntity;

import java.time.Instant;
import java.util.List;

@Mapper
public interface TokenRefreshMapper {

    List<TokenRefreshEntity> selectByToken(String token);

    int insert(TokenRefreshEntity entity);

    int deleteExpiredTokens(Instant expiration);

    int deleteByToken(String token);

    int deleteByUsername(String username);
}