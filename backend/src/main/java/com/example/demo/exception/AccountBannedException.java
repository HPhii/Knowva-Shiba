package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountBannedException extends RuntimeException {
    public AccountBannedException(String message) {
        super(message);
    }
}
