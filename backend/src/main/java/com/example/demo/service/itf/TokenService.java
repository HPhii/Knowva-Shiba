package com.example.demo.service.itf;

import com.example.demo.model.entity.Account;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenService {
    String generateToken(Account account);
    Account getAccountByToken(String token);
    void invalidateToken(String token);
}
