package com.example.demo.service.impl;

import com.example.demo.exception.AuthException;
import com.example.demo.model.entity.Account;
import com.example.demo.repository.AccountRepository;
import com.example.demo.service.intface.ITokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;
    private final Map<String, LocalDateTime> tokenBlacklist = new ConcurrentHashMap<>();

    private final AccountRepository accountRepository;

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(Account account) {
        String token = Jwts.builder()
                .subject(account.getId()+"")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignKey())
                .compact();
        return token;
    }

    @Override
    public Account getAccountByToken(String token) {
        if (isTokenBlacklisted(token)) {
            throw new AuthException("Token has been invalidated!");
        }

        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String idString = claims.getSubject();
        long id = Long.parseLong(idString);

        return accountRepository.findAccountById(id);
    }

    @Override
    public void invalidateToken(String token) {
        // Blacklist the token with its expiry time.
        tokenBlacklist.put(token, LocalDateTime.now().plusHours(1)); // Optional: token lifespan.
    }

    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.containsKey(token);
    }
}
