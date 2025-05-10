package com.example.demo.service;

import com.example.demo.model.entity.Account;
import com.example.demo.model.enums.LoginProvider;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final AccountRepository accountRepository;
    private final TokenService tokenService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        Account account = accountRepository.findAccountByEmail(email);

        if (account == null) {
            account = new Account();
            account.setEmail(email);
            account.setUsername(oAuth2User.getAttribute("name"));
            account.setLoginProvider(LoginProvider.GOOGLE);
            accountRepository.save(account);
        }

        Map<String, Object> attributes = oAuth2User.getAttributes();
        attributes.put("token", tokenService.generateToken(account));

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "sub");
    }
}
