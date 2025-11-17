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
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;

import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import com.example.demo.specification.PaymentTransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final PayOS payOS;
    private final UserRepository userRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final AccountRepository accountRepository;
    private final IAccountService accountService;
    private final ObjectMapper objectMapper;

    @Value("${server.url}")
    private String serverUrl;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.price}")
    private int price;

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

        String returnUrl = serverUrl + "/api/payment/success";
        String cancelUrl = serverUrl + "/api/payment/cancel";

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name(productName).quantity(1).price((long) price).build();

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount((long) price)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .user(user).orderCode(String.valueOf(orderCode)).amount(price)
                .description(description).createdAt(LocalDateTime.now()).checkoutUrl(data.getCheckoutUrl())
                .status(PaymentTransaction.TransactionStatus.PENDING).build();
        paymentTransactionRepository.save(transaction);
        return new PaymentResponse(data.getCheckoutUrl());
    }

    @Override
    public void handleCancel(String orderCode) {
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found for order code: " + orderCode));
        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS) {
            log.warn("Transaction {} already processed.", orderCode);
            return;
        }
        transaction.setStatus(PaymentTransaction.TransactionStatus.CANCELLED);
        paymentTransactionRepository.save(transaction);
        log.info("Payment transaction {} has been cancelled.", orderCode);
    }

    @Override
    public Page<PaymentTransaction> getAllTransactions(Long userId, String username, String email, String orderCode, PaymentTransaction.TransactionStatus status, Pageable pageable) {
        Specification<PaymentTransaction> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and(PaymentTransactionSpecification.withUserId(userId));
        }
        if (username != null && !username.isBlank()) {
            spec = spec.and(PaymentTransactionSpecification.withUsername(username));
        }
        if (email != null && !email.isBlank()) {
            spec = spec.and(PaymentTransactionSpecification.withEmail(email));
        }
        if (orderCode != null && !orderCode.isBlank()) {
            spec = spec.and(PaymentTransactionSpecification.withOrderCode(orderCode));
        }
        if (status != null) {
            spec = spec.and(PaymentTransactionSpecification.withStatus(status));
        }

        return paymentTransactionRepository.findAll(spec, pageable);
    }

    @Override
    public List<PaymentTransaction> getTransactionsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return paymentTransactionRepository.findByUser(user);
    }

    @Override
    public void handleWebhook(String webhookBody) throws Exception {

        WebhookData webhookData;
        try {
            webhookData = payOS.webhooks().verify(webhookBody);
        } catch (Exception e) {
            log.error("Webhook verification failed!", e);
            throw new SecurityException("Invalid PayOS webhook signature: " + e.getMessage());
        }

        if (!"00".equals(webhookData.getCode())) {
            log.info("Ignoring webhook event: {}", webhookData.getCode());
            return;
        }

        long orderCodeLong = webhookData.getOrderCode();
        String orderCode = String.valueOf(orderCodeLong);

        PaymentTransaction transaction = paymentTransactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found for order code: " + orderCode));

        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS) {
            log.warn("Transaction {} already processed.", orderCode);
            return;
        }

        try {
            PaymentLink paymentLinkData = payOS.paymentRequests().get(orderCodeLong);

            if ("PAID".equals(paymentLinkData.getStatus())) {
                transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
                paymentTransactionRepository.save(transaction);

                Account account = transaction.getUser().getAccount();
                // Nâng cấp tài khoản (logic này giữ nguyên)
                accountService.upgradeToPremium(account.getId());
                log.info("Successfully processed payment for order: {}", orderCode);
            } else {
                transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
                paymentTransactionRepository.save(transaction);
                log.error("Webhook received for order {} but status is not PAID. Current status from PayOS API: {}", orderCode, paymentLinkData.getStatus());
            }
        } catch(Exception e) {
            log.error("Could not confirm payment with PayOS API for order {}. Error: {}", orderCode, e.getMessage());
            throw e;
        }
    }

}