package com.example.demo.config;

import com.example.demo.exception.AccountBannedException;
import com.example.demo.exception.AuthException;
import com.example.demo.model.entity.Account;
import com.example.demo.model.enums.Role;
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

    private final List<String> VERIFIED_EMAIL_PERMISSION = List.of(
            "/api/users/{id}/update",
            "/api/payment/create-payment-link",
            "/api/notifications/**",
            "/api/quiz-sets/generate",
            "/api/quiz-sets/save",
            "/api/quiz-sets/{id}/invite",
            "/api/quiz-attempts/**",
            "/api/flashcard-sets/generate",
            "/api/flashcard-sets/save",
            "/api/flashcard-sets/{flashcardSetId}/exam-mode/submit",
            "/api/flashcard-sets/{flashcardSetId}/generate-quiz",
            "/api/flashcard-sets/{id}/invite",
            "/api/spaced-repetition/**"
    );

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;
    private final ITokenService tokenService;
    private final @Lazy HandlerExceptionResolver handlerExceptionResolver;

    private boolean checkRequiresVerifiedEmail(String uri) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        // Chú ý: Cần điều chỉnh logic này nếu có path variable trùng tên
        // Ví dụ: /api/quiz-sets/{id} và /api/flashcard-sets/{id}
        // AntPathMatcher xử lý tốt trường hợp này.
        return VERIFIED_EMAIL_PERMISSION.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    public String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = getToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Account account = tokenService.getAccountByToken(token);

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

            // check if email verification is required
            if (checkRequiresVerifiedEmail(request.getRequestURI()) &&
                    !Boolean.TRUE.equals(account.getIsVerified()) &&
                    account.getRole() != Role.ADMIN) { // admin can bypass email verification
                handlerExceptionResolver.resolveException(request, response, null,
                        new AuthException("Email verification required for this action."));
                return;
            }

            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String role = claims.get("role", String.class);
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(account, null, authorities);

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (AuthException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
            return;
        } catch (ExpiredJwtException e) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new AuthException("Expired Token!!!"));
            return;
        } catch (MalformedJwtException malformedJwtException) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new AuthException("Invalid Token!!!"));
            return;
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new AuthException("Authentication failed: " + e.getMessage()));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
