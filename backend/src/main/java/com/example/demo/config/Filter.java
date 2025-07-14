package com.example.demo.config;

import com.example.demo.exception.AccountBannedException;
import com.example.demo.model.entity.Account;
import com.example.demo.exception.AuthException;
import com.example.demo.model.enums.Status;
import com.example.demo.service.intface.ITokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
            "/api/send-reset-otp",
            "/api/reset-password",
            "/api/payment/success",
            "/api/payment/cancel",
            "/api/payment/payos-webhook",
            "/api/search/quiz-sets",
            "/api/search/flashcard-sets",
            "/api/search/accounts",
            "/api/quiz-sets/all",
            "/api/quiz-sets/{id}",
            "/api/quiz-sets/category/{category}",
            "/api/flashcard-sets/all",
            "/api/flashcard-sets/{id}",
            "/api/flashcard-sets/category/{category}",
            "/api/feedback",
            "/api/bug-reports"
    );

    private final List<String> VERIFIED_EMAIL_PERMISSION = List.of(
            "/api/users/{id}/update",
            "/api/payment/create-payment-link",
            "/api/notifications/**",
            "/api/quiz-sets/generate",
            "/api/quiz-sets/save",
            "/api/quiz-sets/{id}/invite",
            "/api/quiz-sets/{quizSetId}",
            "/api/quiz-sets/{id}",
            "/api/quiz-attempts/**",
            "/api/flashcard-sets/generate",
            "/api/flashcard-sets/save",
            "/api/flashcard-sets/{flashcardSetId}",
            "/api/flashcard-sets/{id}",
            "/api/flashcard-sets/{flashcardSetId}/exam-mode/submit",
            "/api/flashcard-sets/{flashcardSetId}/generate-quiz",
            "/api/flashcard-sets/{id}/invite",
            "/api/spaced-repetition/**"
    );

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

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

    public boolean checkRequiresVerifiedEmail(String uri) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return VERIFIED_EMAIL_PERMISSION.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
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

        // Bỏ qua kiểm tra token cho các yêu cầu WebSocket
        if (uri.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

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
            
            // Check account status
            if (account.getStatus() == Status.BANNED) {
                handlerExceptionResolver.resolveException(request, response, null,
                        new AccountBannedException("Your account has been banned"));
                return;
            }
            
            if (account.getStatus() == Status.INACTIVE) {
                handlerExceptionResolver.resolveException(request, response, null,
                        new AccountBannedException("Your account is inactive"));
                return;
            }

            if (checkRequiresVerifiedEmail(uri) && !Boolean.TRUE.equals(account.getIsVerified())) {
                handlerExceptionResolver.resolveException(request, response, null,
                        new AuthException("Email verification required"));
                return;
            }
            
            // Lấy role từ token
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String role = claims.get("role", String.class);
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(account, token, authorities);
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