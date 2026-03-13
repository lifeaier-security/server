package com.lifeaier.base.comm.mail.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.lifeaier.base.comm.mail.entity.TokenMailEntity;

import java.time.Instant;

@Mapper
public interface TokenMailMapper {

    TokenMailEntity selectByToken(String token);

    int insert(TokenMailEntity entity);

    int deleteExpiredTokens(Instant expiration);

    int deleteByToken(String token);

    int deleteByUsername(String username);
}