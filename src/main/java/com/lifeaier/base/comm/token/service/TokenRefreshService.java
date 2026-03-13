package com.lifeaier.base.comm.token.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.lifeaier.base.comm.token.entity.TokenRefreshEntity;
import com.lifeaier.base.comm.token.mapper.TokenRefreshMapper;

import java.time.Instant;
import java.util.List;

@Service
public class TokenRefreshService {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshService.class);

    private final TokenRefreshMapper tokenRefreshMapper;

    public TokenRefreshService(TokenRefreshMapper tokenRefreshMapper) {

        this.tokenRefreshMapper = tokenRefreshMapper;
    }

    public TokenRefreshEntity selectByToken(String token) {

        List<TokenRefreshEntity> tokens = tokenRefreshMapper.selectByToken(token);

        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        return tokens.get(0);
    }

    public int insert(TokenRefreshEntity entity) {
        return tokenRefreshMapper.insert(entity);
    }

    // 1 hour
    @Scheduled(fixedRate = 3600000)
    public void deleteExpiredTokens() {
    	Instant expiration = Instant.now();
        log.info("DB refresh token check time: " + expiration);
        tokenRefreshMapper.deleteExpiredTokens(expiration);
    }

    public int deleteByToken(String token) {
        return tokenRefreshMapper.deleteByToken(token);
    }

    // one username must have only one refresh token
    public int deleteByUsername(String username) {
        return tokenRefreshMapper.deleteByUsername(username);
    }
}