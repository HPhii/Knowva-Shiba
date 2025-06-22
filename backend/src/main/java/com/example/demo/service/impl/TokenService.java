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
import java.util.Date;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    private final AccountRepository accountRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(Account account) {
        String token = Jwts.builder()
                .subject(account.getId() + "")
                .claim("role", account.getRole().name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24h
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
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000; // TTL in seconds
            if (ttl > 0) {
                String key = "blacklist:" + token;
                redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.SECONDS);
            }
        } catch (ExpiredJwtException e) {
            // Token đã hết hạn, do nothing
        } catch (Exception e) {
            throw new AuthException("Invalid token format or signature!");
        }
    }

    private boolean isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        return redisTemplate.hasKey(key);
    }
}
