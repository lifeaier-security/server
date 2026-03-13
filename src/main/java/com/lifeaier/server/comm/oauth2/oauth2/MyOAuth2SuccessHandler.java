package com.lifeaier.server.comm.oauth2.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.lifeaier.server.comm.oauth2.dto.MyOAuth2User;
import com.lifeaier.server.comm.token.entity.TokenRefreshEntity;
import com.lifeaier.server.comm.utility.JwtUtil;
import com.lifeaier.server.comm.service.SecretService;
import com.lifeaier.server.comm.token.service.TokenRefreshService;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;

@Component
public class MyOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(MyOAuth2SuccessHandler.class);

    private final JwtUtil jwtUtil;
    private final TokenRefreshService tokenRefreshService;
    private final SecretService secretService;

    @Value("${app.url}")
    private String appUrl;

    public MyOAuth2SuccessHandler(
            JwtUtil jwtUtil,
            TokenRefreshService tokenRefreshService,
    		SecretService secretService) {

        this.jwtUtil = jwtUtil;
        this.tokenRefreshService = tokenRefreshService;
        this.secretService = secretService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.info("onAuthenticationSuccess");

        MyOAuth2User myOAuth2User = (MyOAuth2User) authentication.getPrincipal();

        String username = myOAuth2User.getName();

        //tokenService.deleteByUsername(username);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

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

        response.sendRedirect(appUrl);
    }
}
