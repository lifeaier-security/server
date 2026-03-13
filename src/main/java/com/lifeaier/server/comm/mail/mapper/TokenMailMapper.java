package com.lifeaier.server.comm.mail.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.lifeaier.server.comm.mail.entity.TokenMailEntity;

import java.time.Instant;

@Mapper
public interface TokenMailMapper {

    TokenMailEntity selectByToken(String token);

    int insert(TokenMailEntity entity);

    int deleteExpiredTokens(Instant expiration);

    int deleteByToken(String token);

    int deleteByUsername(String username);
}