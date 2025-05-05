package com.example.demo.exception;

public class DuplicateEntity extends RuntimeException {
    public DuplicateEntity(String message) {
        super(message);
    }
}