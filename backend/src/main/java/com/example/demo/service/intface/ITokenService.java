package com.example.demo.service.intface;

import com.example.demo.model.entity.Account;

public interface ITokenService {
    String generateToken(Account account);
    Account getAccountByToken(String token);
    void invalidateToken(String token);
}

