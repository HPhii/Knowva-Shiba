package com.example.demo.exception;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
