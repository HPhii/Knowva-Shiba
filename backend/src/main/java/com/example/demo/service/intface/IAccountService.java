package com.example.demo.service.intface;

import com.example.demo.model.entity.Account;
import com.example.demo.model.io.response.object.AccountResponse;

import java.util.List;

public interface IAccountService {
    List<Account> getAllAccount();
    Account getCurrentAccount();
    AccountResponse getCurrentAccountResponse();
    void sendResetOtp(String email);
    void resetPassword(String email, String otp, String newPassword);
    void sendVerifyOtp(String email);
    void verifyEmail(String email, String otp);
    long getLoggedInAccountId(String email);
    void banUser(Long accountId);
    void upgradeToPremium(Long accountId);
}
