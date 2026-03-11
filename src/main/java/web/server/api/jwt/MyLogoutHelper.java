package web.server.api.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import web.server.api.entity.TokenEntity;
import web.server.api.service.TokenService;

@Component
public class MyLogoutHelper {

    private static final Logger log = LoggerFactory.getLogger(MyLogoutFilter.class);

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    public MyLogoutHelper(JwtUtil jwtUtil,
                          TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
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
        TokenEntity tokenEntity = tokenService.selectByToken(clientRefreshToken);
        if (tokenEntity == null) {
            log.info("invalid refresh token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String serverRefreshToken = tokenEntity.getToken();

        // if token is EXPIRED, return.
        try {

            jwtUtil.isExpired(serverRefreshToken);

        } catch (ExpiredJwtException e) {
            log.info("expired refresh token");
            tokenService.deleteByToken(serverRefreshToken);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 로그아웃 진행
        tokenService.deleteByToken(serverRefreshToken);

        // set client refresh-token null
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
