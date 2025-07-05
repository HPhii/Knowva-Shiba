package com.example.demo.service.scheduler;

import com.example.demo.model.entity.Account;
import com.example.demo.model.enums.Role;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VipExpirationScheduler {
    private final AccountRepository accountRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkVipExpiration() {
        LocalDateTime now = LocalDateTime.now();
        List<Account> vipAccounts = accountRepository.findByRole(Role.VIP);
        for (Account account : vipAccounts) {
            if (now.isAfter(account.getVipEndDate().plusDays(3))) {
                account.setRole(Role.REGULAR);
                account.setVipStartDate(null);
                account.setVipEndDate(null);
                accountRepository.save(account);
            }
        }
    }
}
