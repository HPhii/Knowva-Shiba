package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//this annotation notice that this class is use to catching exception
@RestControllerAdvice
public class ValidationHandler {

    //nếu gặp lỗi MethodArgumentNotValidException thì hàm này sẽ được chạy
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handlerValidation(MethodArgumentNotValidException exception) {
        String message = "";
        for(FieldError fieldError: exception.getBindingResult().getFieldErrors()) {
            message += fieldError.getField() + ": " + fieldError.getDefaultMessage() + "\n";
        }
        return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handlerValidation(Exception exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
