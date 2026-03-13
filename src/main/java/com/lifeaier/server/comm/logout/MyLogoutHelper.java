package com.lifeaier.server.comm.logout;

import com.lifeaier.server.comm.utility.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.lifeaier.server.comm.token.entity.TokenRefreshEntity;
import com.lifeaier.server.comm.token.service.TokenRefreshService;

@Component
public class MyLogoutHelper {

    private static final Logger log = LoggerFactory.getLogger(MyLogoutFilter.class);

    private final JwtUtil jwtUtil;
    private final TokenRefreshService tokenRefreshService;

    public MyLogoutHelper(JwtUtil jwtUtil,
                          TokenRefreshService tokenRefreshService) {
        this.jwtUtil = jwtUtil;
        this.tokenRefreshService = tokenRefreshService;
    }

    public void logout(HttpServletRequest request,
                       HttpServletResponse response) {

        // clear security context
        SecurityContextHolder.clearContext();

        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            log.info("cookie is null");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String clientRefreshToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                clientRefreshToken = cookie.getValue();
            }
        }

        if (clientRefreshToken == null) {
            log.info("refresh token is null");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // DB에 저장되어 있는지 확인
        TokenRefreshEntity tokenRefreshEntity = tokenRefreshService.selectByToken(clientRefreshToken);
        if (tokenRefreshEntity == null) {
            log.info("invalid refresh token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String serverRefreshToken = tokenRefreshEntity.getToken();

        // if token is EXPIRED, return.
        try {

            jwtUtil.isExpired(serverRefreshToken);

        } catch (ExpiredJwtException e) {
            log.info("expired refresh token");
            tokenRefreshService.deleteByToken(serverRefreshToken);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 로그아웃 진행
        tokenRefreshService.deleteByToken(serverRefreshToken);

        // set client refresh-token null
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
