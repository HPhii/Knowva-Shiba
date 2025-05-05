package com.example.demo.exception;

public class PasswordMismatchEntity extends RuntimeException {
    public PasswordMismatchEntity(String message) {
        super(message);
    }
}
