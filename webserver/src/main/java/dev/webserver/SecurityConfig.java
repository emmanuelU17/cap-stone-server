package dev.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.webserver.jwt.RefreshTokenFilter;
import dev.webserver.exception.ExceptionResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static dev.webserver.enumeration.RoleEnum.NATIVE;
import static dev.webserver.enumeration.RoleEnum.WORKER;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;
    @Value(value = "${server.servlet.session.cookie.same-site}")
    private String SAMESITE;
    @Value(value = "${cors.ui.domain}")
    private String CORSDOMAIN;
    @Value("/${api.endpoint.baseurl}")
    private String BASEURL;
    @Value("${spring.profiles.active}")
    private String PROFILE;

    @Bean
    public AuthenticationProvider provider(UserDetailsService service, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(service);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider provider,
            @Qualifier(value = "authenticationEventPublisher") AuthenticationEventPublisher publisher
    ) {
        var manager = new ProviderManager(provider);
        manager.setAuthenticationEventPublisher(publisher);
        manager.setEraseCredentialsAfterAuthentication(true);
        return manager;
    }

    /**
     * Needed as we are sending jwt token as a http token
     * @see
     * <a href="https://docs.spring.io/spring-session/reference/guides/java-custom-cookie.html">documentation</a>
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        var serializer = new DefaultCookieSerializer();
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return serializer;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(15);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(this.CORSDOMAIN));
        config.setAllowedMethods(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(CONTENT_TYPE, ACCEPT, "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Security filter chain responsible for upholding app security
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ObjectMapper mapper,
            RefreshTokenFilter refreshTokenFilter,
            JwtAuthenticationConverter converter
    ) throws Exception {

        if (PROFILE.equals("native-test")) {
            http.csrf(AbstractHttpConfigurer::disable);
        } else {
            var csrfTokenRepository = csrfRepo.apply(this.COOKIESECURE, this.SAMESITE);
            // CSRF Config
            // https://docs.spring.io/spring-security/reference/5.8/migration/servlet/exploits.html
            http
                    .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher
                                    .antMatcher(HttpMethod.POST, this.BASEURL + "payment/webhook"))
                            .csrfTokenRepository(csrfTokenRepository)
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                    .addFilterAfter(new CookieCsrfFilter(), BasicAuthenticationFilter.class);
        }

        final String[] pubRoutes = {
                "/actuator/**",
                this.BASEURL + "csrf",
                this.BASEURL + "client/**",
                this.BASEURL + "worker/auth/login",
                this.BASEURL + "cart/**",
                this.BASEURL + "payment/**",
                this.BASEURL + "checkout/**"
        };

        return http

                // Cors Config
                .cors(withDefaults())

                // Public routes
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(pubRoutes).permitAll();

                    auth.requestMatchers(BASEURL + "cron/**").hasRole(NATIVE.name());
                    auth.requestMatchers(BASEURL + "native/**").hasRole(NATIVE.name());

                    auth.requestMatchers(BASEURL + "shipping/**").hasRole(WORKER.name());
                    auth.requestMatchers(BASEURL + "tax/**").hasRole(WORKER.name());
                    auth.requestMatchers(BASEURL + "worker/**").hasRole(WORKER.name());
                    auth.requestMatchers(BASEURL + "auth/worker").hasRole(WORKER.name());

                    auth.anyRequest().authenticated();
                })

                // Jwt
                .addFilterBefore(refreshTokenFilter, BearerTokenAuthenticationFilter.class)
                // https://docs.spring.io/spring-security/reference/6.0/servlet/oauth2/resource-server/jwt.html
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))

                // Session Management
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))

                // global exception handing
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) -> {
                            final String str = mapper.writeValueAsString(new ExceptionResponse(exception.getMessage(), UNAUTHORIZED));
                            response.setStatus(UNAUTHORIZED.value());
                            response.getWriter().write(str);
                            response.flushBuffer();
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            final String str = mapper.writeValueAsString(new ExceptionResponse("Access Denied", FORBIDDEN));
                            response.setStatus(FORBIDDEN.value());
                            response.getWriter().write(str);
                            response.flushBuffer();
                        }))

                // Logout
                // https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html
                .logout((logoutConfig) -> logoutConfig
                        .logoutUrl(this.BASEURL + "logout")
                        .deleteCookies(this.JSESSIONID)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(OK))
                )
                .build();
    }

    /**
     * Reason for Consumer<ResponseCookie.ResponseCookieBuilder> as per docs secure, domain
     * name and path are deprecated.
     * @see
     * <a href="https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.java">documentation</a>
     */
    static final BiFunction<Boolean, String, CookieCsrfTokenRepository> csrfRepo = (secure, sameSite) -> {
        final Consumer<ResponseCookie.ResponseCookieBuilder> consumer = (cookie) -> cookie
                .httpOnly(false)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(-1);

        var csrf = new CookieCsrfTokenRepository();
        csrf.setCookieCustomizer(consumer);
        return csrf;
    };

}