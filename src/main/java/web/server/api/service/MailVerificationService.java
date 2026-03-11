package web.server.api.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import web.server.api.entity.MailVerificationEntity;
import web.server.api.entity.TokenEntity;
import web.server.api.entity.UserEntity;
import web.server.api.jwt.JwtUtil;
import web.server.api.mapper.MailVerificationMapper;
import web.server.api.mapper.UserMapper;
import web.server.api.utility.MailVerificationUtility;

import java.time.Instant;

@Service
public class MailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(MailVerificationService.class);

    private final MailVerificationMapper mailVerificationMapper;
    private final UserMapper userMapper;

    private final JwtUtil jwtUtil;

    private final TokenService tokenService;
    private final SecretService secretService;

    private final MailService mailService;

    @Value("${app.url}")
    private String appUrl;

    public MailVerificationService(
            MailVerificationMapper mailVerificationMapper,
            UserMapper userMapper,
            JwtUtil jwtUtil,
            TokenService tokenService,
            SecretService secretService,
            MailService mailService) {

        this.mailVerificationMapper = mailVerificationMapper;
        this.userMapper = userMapper;

        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.secretService = secretService;

        this.mailService = mailService;
    }

    public MailVerificationEntity selectByToken(String token) {
        return mailVerificationMapper.selectByToken(token);
    }

    public int insert(MailVerificationEntity entity) {
        return mailVerificationMapper.insert(entity);
    }

    // 1 hour
    @Scheduled(fixedRate = 3600000)
    public void deleteExpiredTokens() {
    	Instant expiration = Instant.now();
        log.info("DB mail verification token check time: " + expiration);
        mailVerificationMapper.deleteExpiredTokens(expiration);
    }

    public int deleteByToken(String token) {
        return mailVerificationMapper.deleteByToken(token);
    }

    public boolean verify(
            String token,
            HttpServletResponse response) {

        MailVerificationEntity mailVerificationEntity = mailVerificationMapper.selectByToken(token);

        if (mailVerificationEntity == null) {
            return false;
        }

        String username = mailVerificationEntity.getUsername();

        mailVerificationMapper.deleteByUsername(username);

        userMapper.verify(username);

        log.info(username + " mail is verified");

        //tokenService.deleteByUsername(username);

        String role = "ROLE_USER";

        String newAccessToken = jwtUtil.createJwt("access", username, role, secretService.getJwtAccess());
        String newRefreshToken = jwtUtil.createJwt("refresh", username, role, secretService.getJwtRefresh());

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUsername(username);
        tokenEntity.setToken(newRefreshToken);
        tokenEntity.setExpiration(Instant.now().plusMillis(secretService.getJwtRefresh()));
        tokenService.insert(tokenEntity);

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

        MailVerificationEntity entity = new MailVerificationEntity();
        entity.setUsername(username);
        entity.setToken(token);
        entity.setExpiration(Instant.now().plusMillis(secretService.getMailVerificationTokenExpire()));
        mailVerificationMapper.insert(entity);

        String url = appUrl + "/verify?token=" + token;

        mailService.sendMail(
                email,
                "[SHINE-UG] Mail Verification",
                url
        );
    }
}