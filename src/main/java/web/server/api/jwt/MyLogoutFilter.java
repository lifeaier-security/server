package web.server.api.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

import web.server.api.entity.TokenEntity;
import web.server.api.service.TokenService;

import java.io.IOException;

public class MyLogoutFilter extends GenericFilterBean {

    private static final Logger log = LoggerFactory.getLogger(MyLogoutFilter.class);

    private final MyLogoutHelper myLogoutHelper;

    public MyLogoutFilter(MyLogoutHelper myLogoutHelper) {

        this.myLogoutHelper = myLogoutHelper;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request,
                          HttpServletResponse response,
                          FilterChain filterChain)
            throws IOException, ServletException {

        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/logout$")) {
            log.info("filter for logout");
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            log.info("logout method is wrong");
            filterChain.doFilter(request, response);
            return;
        }

        myLogoutHelper.logout(request, response);
    }
}