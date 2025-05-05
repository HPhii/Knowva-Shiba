package com.example.demo.service.itf;

import com.example.demo.model.io.request.LoginRequest;
import com.example.demo.model.io.request.RegisterRequest;
import com.example.demo.model.io.response.object.AccountResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AuthenticationService {
    AccountResponse register(RegisterRequest registerRequestDTO);
    AccountResponse login(LoginRequest loginRequestDTO);
    AccountResponse loginGoogle(String googleToken);
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;
}
