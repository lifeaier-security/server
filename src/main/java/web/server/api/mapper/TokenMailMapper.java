package web.server.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import web.server.api.entity.TokenMailEntity;

import java.time.Instant;

@Mapper
public interface TokenMailMapper {

    TokenMailEntity selectByToken(String token);

    int insert(TokenMailEntity entity);

    int deleteExpiredTokens(Instant expiration);

    int deleteByToken(String token);

    int deleteByUsername(String username);
}