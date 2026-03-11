package web.server.api.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SecretService {

    private Long jwtAccess;
    private Long jwtRefresh;
    private int jwtRefreshCookie;
    private Long mailVerificationTokenExpire;

    public SecretService(
            @Value("${jwt.access}")Long jwtAccess,
            @Value("${jwt.refresh}")Long jwtRefresh,
            @Value("${jwt.refresh.cookie}")int jwtRefreshCookie,
            @Value("${mail.verification.token.expire}")Long mailVerificationTokenExpire) {

        this.jwtAccess = jwtAccess;
        this.jwtRefresh = jwtRefresh;
        this.jwtRefreshCookie = jwtRefreshCookie;
        this.mailVerificationTokenExpire = mailVerificationTokenExpire;
    }
}