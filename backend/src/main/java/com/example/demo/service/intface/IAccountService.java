package com.example.demo.service.intface;

import com.example.demo.model.entity.Account;
import com.example.demo.model.io.response.object.AccountResponse;

import java.util.List;

public interface IAccountService {
    List<Account> getAllAccount();
    Account getCurrentAccount();
    AccountResponse getCurrentAccountResponse();
}
