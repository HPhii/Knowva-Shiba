package com.example.demo.service.impl;

import com.example.demo.exception.AuthException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.model.entity.Account;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.Status;
import com.example.demo.model.io.response.object.AccountResponse;
import com.example.demo.model.io.response.object.EmailDetails;
import com.example.demo.repository.AccountRepository;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IEmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final IEmailService emailService;

    @Override
    @CacheEvict(value = "userProfile", key = "#id")
    public void banUser(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + id));
        account.setStatus(Status.BANNED);
        accountRepository.save(account);
    }

    @Override
    @CacheEvict(value = "userProfile", key = "#accountId")
    public void upgradeToPremium(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
        account.setRole(Role.VIP);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxVipEndDate = now.plusMonths(12);
        if (account.getVipEndDate() != null && account.getVipEndDate().isAfter(now)) {
            LocalDateTime proposedEndDate = account.getVipEndDate().plusMonths(1);
            if (proposedEndDate.isAfter(maxVipEndDate)) {
                account.setVipEndDate(maxVipEndDate);
            } else {
                account.setVipEndDate(proposedEndDate);
            }
        } else {
            account.setVipStartDate(now);
            account.setVipEndDate(now.plusMonths(1));
        }
        accountRepository.save(account);
    }

    @Override
    public List<Account> getAllAccount() {
        return accountRepository.findAll();
    }

    @Override
    public Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Account) {
            Account currentAccount = (Account) principal;
            return accountRepository.findById(currentAccount.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + currentAccount.getId()));
        } else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }
    }

    @Override
    public AccountResponse getCurrentAccountResponse() {
        Account account = this.getCurrentAccount();
        return accountMapper.toAccountResponse(account);
    }

    @Override
    public void sendResetOtp(String email) {
        sendOtp(email, "Reset Password OTP", "reset");
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found" + email));

        if (account.getResetOtp() == null || !account.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (account.getResetOtpExpiredAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP expired");
        }

        //update password
        account.setPassword(passwordEncoder.encode(newPassword));
        account.setResetOtp(null);
        account.setResetOtpExpiredAt(0L);
        accountRepository.save(account);
    }

    @Override
    public void sendVerifyOtp(String email) {
        sendOtp(email, "Verify Email OTP", "verify");
    }

    @Override
    public void verifyEmail(String email, String otp) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found" + email));

        if (account.getVerifyOtp() == null || !account.getVerifyOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (account.getVerifyOtpExpiredAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP expired");
        }

        //update verify
        account.setIsVerified(true);
        account.setVerifyOtp(null);
        account.setVerifyOtpExpiredAt(0L);
        accountRepository.save(account);
    }

    @Override
    public long getLoggedInAccountId(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Account not found" + email));
        return account.getId();
    }

    private void sendOtp(String email, String subject, String type) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found: " + email));

        if ("verify".equals(type) && Boolean.TRUE.equals(account.getIsVerified())) {
            throw new RuntimeException("Account already verified");
        }

        // Generate OTP
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        long expirationTime = System.currentTimeMillis() + 15 * 60 * 1000L;

        // Set OTP fields
        if ("verify".equals(type)) {
            account.setVerifyOtp(otp);
            account.setVerifyOtpExpiredAt(expirationTime);
        } else if ("reset".equals(type)) {
            account.setResetOtp(otp);
            account.setResetOtpExpiredAt(expirationTime);
        } else {
            throw new IllegalArgumentException("Unsupported OTP type: " + type);
        }

        accountRepository.save(account);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setReceiver(account);
        emailDetails.setSubject(subject);

        Map<String, Object> context = Map.of(
                "otp", otp,
                "name", account.getUsername(),
                "exptime", ((expirationTime - System.currentTimeMillis()) / (60 * 1000)) + 1
        );

        try {
            emailService.sendMail(emailDetails, "otp.html", context);
        } catch (MessagingException e) {
            // Xử lý lỗi nếu cần, hoặc để @Async tự xử lý
            e.printStackTrace();
        }
    }
}
