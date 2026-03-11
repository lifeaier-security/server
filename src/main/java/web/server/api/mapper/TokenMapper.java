package web.server.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import web.server.api.entity.TokenEntity;

import java.time.Instant;
import java.util.List;

@Mapper
public interface TokenMapper {

    List<TokenEntity> selectByToken(String token);

    int insert(TokenEntity entity);

    int deleteExpiredTokens(Instant expiration);

    int deleteByToken(String token);

    int deleteByUsername(String username);
}