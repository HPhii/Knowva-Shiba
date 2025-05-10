package com.example.demo.service;

import com.example.demo.model.entity.Account;
import com.example.demo.model.io.response.object.AccountResponse;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> getAllAccount() {
        return accountRepository.findAll();
    }

    public Account getCurrentAccount() {
        Account currentAccount = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findAccountById(currentAccount.getId());
    }

    public AccountResponse getCurrentAccountResponse() {
        Account currentAccount = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountRepository.findAccountById(currentAccount.getId());

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setAccountId(account.getId());
        accountResponse.setEmail(account.getEmail());
        accountResponse.setIsVerified(account.getIsVerified());
        accountResponse.setUserId(account.getUser().getId());

        return accountResponse;
    }
}
