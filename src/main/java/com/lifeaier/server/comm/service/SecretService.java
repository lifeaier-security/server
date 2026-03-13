package com.lifeaier.server.comm.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SecretService {

    private Long jwtAccess;
    private Long jwtRefresh;
    private int jwtRefreshCookie;
    private Long tokenMailExpire;

    public SecretService(
            @Value("${jwt.access}")Long jwtAccess,
            @Value("${jwt.refresh}")Long jwtRefresh,
            @Value("${jwt.refresh.cookie}")int jwtRefreshCookie,
            @Value("${token.mail.expire}")Long tokenMailExpire) {

        this.jwtAccess = jwtAccess;
        this.jwtRefresh = jwtRefresh;
        this.jwtRefreshCookie = jwtRefreshCookie;
        this.tokenMailExpire = tokenMailExpire;
    }
}