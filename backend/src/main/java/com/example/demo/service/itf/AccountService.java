package com.example.demo.service.itf;

import com.example.demo.model.entity.Account;
import com.example.demo.model.io.response.object.AccountResponse;
import jakarta.mail.MessagingException;

import java.util.List;

public interface AccountService {
    List<Account> getAllAccount();
    Account getCurrentAccount();
    AccountResponse getCurrentAccountResponse();
}
