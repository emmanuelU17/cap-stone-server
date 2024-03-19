<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/auth/config/CookieCsrfFilter.java
package dev.webserver.auth.config;
========
package dev.capstone.auth.config;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/auth/config/CookieCsrfFilter.java

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security 6 doesn't set a XSRF-TOKEN cookie by default.
 * This solution is
 * <a href="https://github.com/spring-projects/spring-security/issues/12141#issuecomment-1321345077">
 * recommended by Spring Security.</a>
 * <a href="https://docs.spring.io/spring-security/reference/5.8/migration/servlet/exploits.html">...</a>
 */
public class CookieCsrfFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
        filterChain.doFilter(request, response);
    }

}