package com.example.demo.config;

import com.example.demo.model.entity.Account;
import com.example.demo.exception.AuthException;
import com.example.demo.service.intface.ITokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Filter extends OncePerRequestFilter {
    private final List<String> AUTH_PERMISSION = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/register",
            "/api/login",
            "/api/google",
            "/api/send-verify-otp",
            "/api/verify-email",
            "/api/send-reset-otp",
            "/api/reset-password"
    );

    private final ITokenService tokenService;

//    @Autowired
//    @Qualifier("handlerExceptionResolver")
    private final @Lazy HandlerExceptionResolver handlerExceptionResolver;

    public boolean checkIsPublicAPI(String uri) {
        //uri: /api/register

        // nếu gặp những API trong List AUTH_PERMISSION -> cho phép truy cập -> true
        // else -> false
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return AUTH_PERMISSION.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    public String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.substring(7);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        boolean isPublicAPI = checkIsPublicAPI(uri);

        // Nếu là API công khai, không cần xác thực.
        if (isPublicAPI) {
            filterChain.doFilter(request, response);
            return;
        }

        // Nếu là API yêu cầu xác thực.
        String token = getToken(request);
        if (token == null) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new AuthException("Empty Token!!!"));
            return;
        }

        try {
            Account account = tokenService.getAccountByToken(token);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(account, token, account.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (AuthException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        } catch (ExpiredJwtException e) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new AuthException("Expired Token!!!"));
        } catch (MalformedJwtException malformedJwtException) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new AuthException("Invalid Token!!!"));
        }
    }
}
