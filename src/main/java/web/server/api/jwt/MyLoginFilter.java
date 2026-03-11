package web.server.api.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;
import web.server.api.common.ErrorCode;
import web.server.api.dto.MyUserDetails;
import web.server.api.entity.TokenEntity;
import web.server.api.service.SecretService;
import web.server.api.service.TokenService;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class MyLoginFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(MyLoginFilter.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private final TokenService tokenService;
    private final SecretService secretService;

    @Value("${app.url}")
    private String appUrl;

    public MyLoginFilter(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            TokenService tokenService,
            SecretService secretService) {

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.secretService = secretService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {

        if (request.getContentType() != null && request.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            try {

                log.info("authenticate");

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);

                String username = credentials.get("username");
                String password = credentials.get("password");

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

                return authenticationManager.authenticate(authToken);

            } catch (IOException e) {
                throw new AuthenticationServiceException("Error reading JSON request", e);
            }
        }

        throw new AuthenticationServiceException("Invalid content type: Expected application/json");
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) throws IOException, ServletException {

        log.info("successfulAuthentication");

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        char verified = userDetails.getVerified();

        if (verified == 'N') {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorCode error = ErrorCode.MAIL_NOT_VERIFIED;

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("error", error.name());

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), responseBody);

            return;
        }

        // IP
        String ipAddress = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(ip -> ip.split(",")[0])
                .orElse(request.getRemoteAddr());

        // Device ID
        String deviceId = UUID.randomUUID().toString();

        String username = userDetails.getUsername();

        //tokenService.deleteByUsername(username);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

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

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        log.info("authentication is failed");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorCode error = ErrorCode.AUTHENTICATION_FAILED;

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", error.name());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), responseBody);
    }
}
