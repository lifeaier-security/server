package web.server.api.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.server.api.entity.TokenEntity;
import web.server.api.jwt.JwtUtil;
import web.server.api.service.SecretService;
import web.server.api.service.TokenService;

import java.time.Instant;

@RestController
@RequestMapping("/token")
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final SecretService secretService;

    public TokenController(
            JwtUtil jwtUtil,
            TokenService tokenService,
            SecretService secretService) {

        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.secretService = secretService;
    }

    @PostMapping("/renew")
    public ResponseEntity<?> renew(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            log.info("cookie is null");
            return new ResponseEntity<>("cookie is null", HttpStatus.FORBIDDEN);
        }

        String clientRefreshToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                clientRefreshToken = cookie.getValue();
            }
        }

        if (clientRefreshToken == null) {
            log.info("refresh token is null");
            return new ResponseEntity<>("refresh token is null", HttpStatus.FORBIDDEN);
        }

        // DB에 저장되어 있는지 확인
        TokenEntity tokenEntity = tokenService.selectByToken(clientRefreshToken);
        if (tokenEntity == null) {
            log.info("invalid refresh token");
            return new ResponseEntity<>("invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        String serverRefreshToken = tokenEntity.getToken();

        // if token is EXPIRED, return.
        try {

            jwtUtil.isExpired(serverRefreshToken);

        } catch (ExpiredJwtException e) {
            log.info("expired refresh token");
            tokenService.deleteByToken(serverRefreshToken);
            return new ResponseEntity<>("refresh token expired", HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.getUsername(clientRefreshToken);
        String role = jwtUtil.getRole(clientRefreshToken);

        String newAccessToken = jwtUtil.createJwt("access", username, role, secretService.getJwtAccess());
        String newRefreshToken = jwtUtil.createJwt("refresh", username, role, secretService.getJwtRefresh());

        // rotate refresh token
        //tokenService.deleteByUsername(username);
        tokenService.deleteByToken(clientRefreshToken);
        tokenEntity = new TokenEntity();
        tokenEntity.setUsername(username);
        tokenEntity.setToken(newRefreshToken);
        tokenEntity.setExpiration(Instant.now().plusMillis(secretService.getJwtRefresh()));
        tokenService.insert(tokenEntity);

        response.setHeader("Authorization", "Bearer " + newAccessToken);

        // create client refresh-token
        Cookie cookie = new Cookie("refresh", newRefreshToken);
        cookie.setMaxAge(secretService.getJwtRefreshCookie());
        //cookie.setSecure(true);	// use case is https
        cookie.setPath("/");		// Бүх эндпойнт дээр илгээгдэх
        cookie.setHttpOnly(true);	// cannot use cookie in java script
        response.addCookie(cookie);

        log.info("access token is reissued, and refresh token is rotated.");

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(secretService.getJwtRefreshCookie());
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}