package web.server.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import web.server.api.jwt.JwtUtil;
import web.server.api.jwt.MyJwtFilter;
import web.server.api.jwt.MyLoginFilter;
import web.server.api.jwt.MyLogoutFilter;
import web.server.api.jwt.MyLogoutHelper;
import web.server.api.oauth2.MyClientRegistrationRepository;
import web.server.api.oauth2.MyOAuth2AuthorizedClientService;
import web.server.api.oauth2.MyOAuth2SuccessHandler;
import web.server.api.service.MyOAuth2UserService;
import web.server.api.service.SecretService;
import web.server.api.service.TokenService;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.url}")
    private String appUrl;

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final MyLogoutHelper myLogoutHelper;

    private final MyOAuth2UserService myOAuth2UserService;
    private final MyOAuth2SuccessHandler myOAuth2SuccessHandler;

    private final MyClientRegistrationRepository myClientRegistrationRepository;
    private final MyOAuth2AuthorizedClientService myOAuth2AuthorizedClientService;
    private final JdbcTemplate jdbcTemplate;

    private final AuthenticationConfiguration authenticationConfiguration;
    private final SecretService secretService;

    public SecurityConfig(
            // token related
            JwtUtil jwtUtil,
            TokenService tokenService,
            MyLogoutHelper myLogoutHelper,
            // oauth2 related
            MyOAuth2UserService myOAuth2UserService,
            MyOAuth2SuccessHandler myOAuth2SuccessHandler,
            // social client registration
            MyClientRegistrationRepository myClientRegistrationRepository,
            MyOAuth2AuthorizedClientService myOAuth2AuthorizedClientService,
            JdbcTemplate jdbcTemplate,
            // local authentication
            AuthenticationConfiguration authenticationConfiguration,
            SecretService secretService) {

        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.myLogoutHelper = myLogoutHelper;

        this.myOAuth2UserService = myOAuth2UserService;
        this.myOAuth2SuccessHandler = myOAuth2SuccessHandler;

        this.myClientRegistrationRepository = myClientRegistrationRepository;
        this.myOAuth2AuthorizedClientService = myOAuth2AuthorizedClientService;
        this.jdbcTemplate = jdbcTemplate;

        this.authenticationConfiguration = authenticationConfiguration;
        this.secretService = secretService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptoPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList(appUrl));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

                        return configuration;
                    }
                }));

        http.csrf((auth) -> auth.disable());

        http.formLogin((auth) -> auth.disable());

        http.httpBasic((auth) -> auth.disable());

        http.oauth2Login((oauth2) -> oauth2
                .clientRegistrationRepository(myClientRegistrationRepository.clientRegistrationRepository())
                .authorizedClientService(myOAuth2AuthorizedClientService.oAuth2AuthorizedClientService(jdbcTemplate,
                        myClientRegistrationRepository.clientRegistrationRepository()))
                .userInfoEndpoint((config) -> config.userService(myOAuth2UserService))
                .successHandler(myOAuth2SuccessHandler)
                // ADD THIS: Point the login page to something that doesn't exist
                // or your React login route to stop the auto-generation
                .loginPage(appUrl + "/login")
        );

        http.exceptionHandling(e -> e
                // ADD THIS: Return 401 instead of redirecting to the default login page
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        );

        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                .requestMatchers("/token/renew").permitAll()
                .requestMatchers("/join", "/verify", "/resend").permitAll()
                .anyRequest().authenticated());

        http.addFilterAt(new MyLoginFilter(
                authenticationManager(authenticationConfiguration),
                jwtUtil,
                tokenService,
                secretService), UsernamePasswordAuthenticationFilter.class);

        http.addFilterAfter(new MyJwtFilter(jwtUtil), MyLoginFilter.class);

        http.addFilterBefore(new MyLogoutFilter(myLogoutHelper), LogoutFilter.class);

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
