package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.PaymentTransaction;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.model.io.response.object.PaymentResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.PaymentTransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final PayOS payOS;
    private final UserRepository userRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final AccountRepository accountRepository;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public PaymentResponse createPaymentLink(Long userId, boolean isRenewal) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Account account = user.getAccount();
        if (!isRenewal && account.getRole() == Role.VIP) {
            throw new IllegalStateException("User is already VIP");
        }
        if (isRenewal && account.getRole() != Role.VIP) {
            throw new IllegalStateException("User is not VIP");
        }

        long orderCode = System.currentTimeMillis() * 1000 + new Random().nextInt(1000);
        String productName = isRenewal ? "Renew VIP" : "Upgrade VIP";
        String description = orderCode + " - " + (isRenewal ? "RVIP" : "UVIP");
        int price = 5000; // 5000 to test
        String returnUrl = clientUrl + "/success";
        String cancelUrl = clientUrl + "/cancel";

        ItemData item = ItemData.builder()
                .name(productName)
                .quantity(1)
                .price(price)
                .build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(price)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        CheckoutResponseData data = payOS.createPaymentLink(paymentData);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .user(user)
                .orderCode(String.valueOf(orderCode))
                .amount(price)
                .description(description)
                .createdAt(LocalDateTime.now())
                .status(PaymentTransaction.TransactionStatus.PENDING)
                .build();
        paymentTransactionRepository.save(transaction);

        return new PaymentResponse(data.getCheckoutUrl());
    }

    @Override
    public void handlePaymentSuccess(String orderCode) throws Exception {
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS) {
            return;
        }

        PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(Long.parseLong(orderCode));
        if ("PAID".equals(paymentLinkData.getStatus())) {
            transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            paymentTransactionRepository.save(transaction);

            Account account = transaction.getUser().getAccount();
            LocalDateTime now = LocalDateTime.now();
            if (account.getRole() == Role.VIP && account.getVipEndDate() != null) {
                account.setVipEndDate(account.getVipEndDate().plusDays(30));
            } else {
                account.setRole(Role.VIP);
                account.setVipStartDate(now);
                account.setVipEndDate(now.plusDays(30));
            }
            accountRepository.save(account);
        }
    }

    @Override
    public void handlePaymentCancel(String orderCode) throws Exception {
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.CANCELLED) {
            return;
        }

        transaction.setStatus(PaymentTransaction.TransactionStatus.CANCELLED);
        paymentTransactionRepository.save(transaction);
    }

    @Override
    public List<PaymentTransaction> getAllTransactions() {
        return paymentTransactionRepository.findAll();
    }
}
