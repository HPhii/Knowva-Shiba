package com.example.demo.service;

import com.example.demo.exception.DuplicateEntity;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.exception.PasswordMismatchEntity;
import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.LoginProvider;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.Status;
import com.example.demo.model.io.request.LoginRequest;
import com.example.demo.model.io.request.RegisterRequest;
import com.example.demo.model.io.response.object.AccountResponse;
import com.example.demo.model.io.response.object.EmailDetails;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;


    private static final String WELCOME_SUBJECT = "ABC";
    private static final String WELCOME_TEMPLATE = "welcome-template";

    public AccountResponse register(RegisterRequest registerRequestDTO) {
        if (!registerRequestDTO.getPassword().equals(registerRequestDTO.getConfirmPassword())) {
            throw new PasswordMismatchEntity("Passwords do not match!");
        }

        if (accountRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new DuplicateEntity("This email is already registered!!!");
        }

        if (accountRepository.existsByUsername(registerRequestDTO.getUsername())) {
            throw new DuplicateEntity("This username is already used by others!!");
        }

        User newUser = new User();
        userRepository.save(newUser);

        Account account = Account.builder()
                .username(registerRequestDTO.getUsername())
                .email(registerRequestDTO.getEmail())
                .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .status(Status.ACTIVE)
                .role(Role.USER)
                .loginProvider(LoginProvider.EMAIL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(newUser)
                .isVerified(false)
                .build();

        Account newAccount = accountRepository.save(account);

        AccountResponse accountResponse = AccountResponse.builder()
                .accountId(newAccount.getId())
                .email(newAccount.getEmail())
                .username(newAccount.getUsername())
                .userId(newUser.getId())
                .isVerified(newAccount.getIsVerified())
                .build();

        sendMail(newAccount);

        return accountResponse;
    }


    private void sendMail(Account newAccount) {
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setReceiver(newAccount);
        emailDetails.setSubject(WELCOME_SUBJECT);
        Map<String, Object> welcomeContext = Map.of("name", emailDetails.getReceiver().getEmail());
        try {
            emailService.sendMail(emailDetails, WELCOME_TEMPLATE, welcomeContext);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public AccountResponse login(LoginRequest loginRequestDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequestDTO.getEmail(),
                    loginRequestDTO.getPassword()
            ));

            // => tài khoản có tồn tại
            Account account = (Account) authentication.getPrincipal();

            return AccountResponse.builder()
                    .accountId(account.getId())
                    .username(account.getUsername())
                    .email(account.getEmail())
                    .role(account.getRole())
                    .isVerified(account.getIsVerified())
                    .userId(account.getUser() != null ? account.getUser().getId() : null)
                    .token(tokenService.generateToken(account))
                    .build();
        } catch (Exception e) {
            throw new EntityNotFoundException("Incorrect Username or Password");
        }
    }

    private String generateUniqueUsername(String googleName) {
        String normalized = Normalizer.normalize(googleName, Normalizer.Form.NFD);
        String username = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        username = username.replaceAll("\\s+", "").toLowerCase();

        String uniqueUsername = username;
        int counter = 1;
        while (accountRepository.existsByUsername(uniqueUsername)) {
            uniqueUsername = username + counter;
            counter++;
        }

        return uniqueUsername;
    }

    public AccountResponse loginGoogle(String googleToken) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList("472892753586-grlnbpao8omb8dr1hfk57o87iujm54dg.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(googleToken);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Google token");
        }

        if (idToken == null) throw new RuntimeException("Invalid ID token.");

        String email = idToken.getPayload().getEmail();
        Account account = accountRepository.findAccountByEmail(email);

        if (account == null) {
            String name = (String) idToken.getPayload().get("name");
            String username = generateUniqueUsername(name);

            User newUser = new User();
            newUser.setFullName(name);
            userRepository.save(newUser);

            account = Account.builder()
                    .email(email)
                    .username(username)
                    .loginProvider(LoginProvider.GOOGLE)
                    .role(Role.USER)
                    .status(Status.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .user(newUser)
                    .build();
            accountRepository.save(account);
        }

        AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);
        accountResponse.setToken(tokenService.generateToken(account));
        return accountResponse;
    }
}
