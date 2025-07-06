package com.example.demo.service.impl;

import com.example.demo.exception.AccountBannedException;
import com.example.demo.exception.DuplicateEntity;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.exception.PasswordMismatchEntity;
import com.example.demo.mapper.AccountMapper;
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
import com.example.demo.service.intface.IAuthenticationService;
import com.example.demo.service.intface.IEmailService;
import com.example.demo.service.intface.ITokenService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
    private static final String WELCOME_SUBJECT = "ABC";
    private static final String WELCOME_TEMPLATE = "welcome-template";
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final ITokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final AccountMapper accountMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public AccountResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntity("This email is already registered!!!");
        }

        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEntity("This username is already used by others!!");
        }

        User newUser = new User();
        userRepository.save(newUser);

        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(Status.ACTIVE)
                .role(Role.REGULAR)
                .loginProvider(LoginProvider.EMAIL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(newUser)
                .isVerified(false)
                .build();

        Account newAccount = accountRepository.save(account);

//        sendMail(newAccount);
//        AccountResponse accountResponse = AccountResponse.builder()
//                .accountId(newAccount.getId())
//                .email(newAccount.getEmail())
//                .username(newAccount.getUsername())
//                .userId(newUser.getId())
//                .isVerified(newAccount.getIsVerified())
//                .build();


        return accountMapper.toAccountResponse(newAccount);
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

    @Override
    public AccountResponse login(LoginRequest loginRequestDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDTO.getEmail(),
                            loginRequestDTO.getPassword()
                    )
            );
            Account account = (Account) authentication.getPrincipal();
            
            // Check account status
            if (account.getStatus() == Status.BANNED) {
                throw new EntityNotFoundException("Your account has been banned");
            }
            
            if (account.getStatus() == Status.INACTIVE) {
                throw new EntityNotFoundException("Your account is inactive");
            }
            
            // Generate token even for unverified accounts, but client should handle verification state
            String token = generateAndStoreToken(account);
            AccountResponse accountResponse = accountMapper.toAccountResponse(account);
            accountResponse.setToken(token);
            return accountResponse;
        } catch (Exception e) {
            throw new PasswordMismatchEntity("Incorrect Email or Password");
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

    @Override
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
            
            // Get profile picture URL from Google account
            String pictureUrl = (String) idToken.getPayload().get("picture");

            User newUser = new User();
            newUser.setFullName(name);
            newUser.setAvatarUrl(pictureUrl);
            userRepository.save(newUser);

            account = Account.builder()
                    .email(email)
                    .username(username)
                    .loginProvider(LoginProvider.GOOGLE)
                    .role(Role.REGULAR)
                    .status(Status.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .user(newUser)
                    .isVerified(true)
                    .build();
            accountRepository.save(account);
        } else {
            // Check if existing account is banned
            if (account.getStatus() == Status.BANNED) {
                throw new AccountBannedException("Your account has been banned");
            }
        }

        String token = generateAndStoreToken(account);
        AccountResponse accountResponse = accountMapper.toAccountResponse(account);
        accountResponse.setToken(token);
        return accountResponse;
    }

    private String generateAndStoreToken(Account account) {
        String userId = account.getId() + "";
        String sessionKey = "session:" + userId;
        String existingToken = (String) redisTemplate.opsForValue().get(sessionKey);
        if (existingToken != null) {
            tokenService.invalidateToken(existingToken);
        }
        String newToken = tokenService.generateToken(account);
        redisTemplate.opsForValue().set(sessionKey, newToken, 24, TimeUnit.HOURS);
        return newToken;
    }
}