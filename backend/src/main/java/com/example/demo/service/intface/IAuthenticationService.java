package com.example.demo.service.intface;

import com.example.demo.model.io.request.LoginRequest;
import com.example.demo.model.io.request.RegisterRequest;
import com.example.demo.model.io.response.object.AccountResponse;

public interface IAuthenticationService {
    AccountResponse register(RegisterRequest registerRequestDTO);
    AccountResponse login(LoginRequest loginRequestDTO);
    AccountResponse loginGoogle(String googleToken);
}
