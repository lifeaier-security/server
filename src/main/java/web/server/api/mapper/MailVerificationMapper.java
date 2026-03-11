package web.server.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import web.server.api.entity.MailVerificationEntity;

import java.time.Instant;

@Mapper
public interface MailVerificationMapper {

    MailVerificationEntity selectByToken(String token);

    int insert(MailVerificationEntity entity);

    int deleteExpiredTokens(Instant expiration);

    int deleteByToken(String token);

    int deleteByUsername(String username);
}