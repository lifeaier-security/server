package web.server.api.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import web.server.api.entity.TokenMailEntity;
import web.server.api.entity.TokenRefreshEntity;
import web.server.api.entity.UserEntity;
import web.server.api.jwt.JwtUtil;
import web.server.api.mapper.TokenMailMapper;
import web.server.api.mapper.UserMapper;
import web.server.api.utility.MailVerificationUtility;

import java.time.Instant;

@Service
public class MailVerifyService {

    private static final Logger log = LoggerFactory.getLogger(MailVerifyService.class);

    private final TokenMailMapper tokenMailMapper;
    private final UserMapper userMapper;

    private final JwtUtil jwtUtil;

    private final TokenRefreshService tokenRefreshService;
    private final SecretService secretService;

    private final MailSendService mailSendService;

    @Value("${app.url}")
    private String appUrl;

    public MailVerifyService(
            TokenMailMapper tokenMailMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil,
            TokenRefreshService tokenRefreshService,
            SecretService secretService,
            MailSendService mailSendService) {

        this.tokenMailMapper = tokenMailMapper;
        this.userMapper = userMapper;

        this.jwtUtil = jwtUtil;
        this.tokenRefreshService = tokenRefreshService;
        this.secretService = secretService;

        this.mailSendService = mailSendService;
    }

    public TokenMailEntity selectByToken(String token) {
        return tokenMailMapper.selectByToken(token);
    }

    public int insert(TokenMailEntity entity) {
        return tokenMailMapper.insert(entity);
    }

    // 1 hour
    @Scheduled(fixedRate = 3600000)
    public void deleteExpiredTokens() {
    	Instant expiration = Instant.now();
        log.info("DB mail verification token check time: " + expiration);
        tokenMailMapper.deleteExpiredTokens(expiration);
    }

    public int deleteByToken(String token) {
        return tokenMailMapper.deleteByToken(token);
    }

    public boolean verify(
            String token,
            HttpServletResponse response) {

        TokenMailEntity tokenMailEntity = tokenMailMapper.selectByToken(token);

        if (tokenMailEntity == null) {
            return false;
        }

        String username = tokenMailEntity.getUsername();

        tokenMailMapper.deleteByUsername(username);

        userMapper.verify(username);

        log.info(username + " mail is verified");

        //tokenService.deleteByUsername(username);

        String role = "ROLE_USER";

        String newAccessToken = jwtUtil.createJwt("access", username, role, secretService.getJwtAccess());
        String newRefreshToken = jwtUtil.createJwt("refresh", username, role, secretService.getJwtRefresh());

        TokenRefreshEntity tokenRefreshEntity = new TokenRefreshEntity();
        tokenRefreshEntity.setUsername(username);
        tokenRefreshEntity.setToken(newRefreshToken);
        tokenRefreshEntity.setExpiration(Instant.now().plusMillis(secretService.getJwtRefresh()));
        tokenRefreshService.insert(tokenRefreshEntity);

        response.setHeader("Authorization", "Bearer " + newAccessToken);

        // create client refresh-token
        Cookie cookie = new Cookie("refresh", newRefreshToken);
        cookie.setMaxAge(secretService.getJwtRefreshCookie());
        cookie.setSecure(true);	// use case is https
        cookie.setPath("/");		// Бүх эндпойнт дээр илгээгдэх
        cookie.setHttpOnly(true);	// cannot use cookie in java script
        response.addCookie(cookie);

        log.info("tokens are created successfully");

        return true;
    }

    public void resend(String username) {

        UserEntity userEntity = userMapper.selectByUsername(username);

        String email = userEntity.getEmail();
        String token = MailVerificationUtility.generateToken();

        TokenMailEntity entity = new TokenMailEntity();
        entity.setUsername(username);
        entity.setToken(token);
        entity.setExpiration(Instant.now().plusMillis(secretService.getTokenMailExpire()));
        tokenMailMapper.insert(entity);

        String url = appUrl + "/verify?token=" + token;

        mailSendService.sendMail(
                email,
                "Verify Mail",
                url
        );
    }
}