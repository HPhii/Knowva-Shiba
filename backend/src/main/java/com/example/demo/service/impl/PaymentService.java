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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    private final ObjectMapper objectMapper;

    @Value("${server.url}")
    private String serverUrl;

    @Value("${payos.checksum-key}")
    private String checksumKey;

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
        int price = 5000;

        String returnUrl = serverUrl + "/api/payment/success";
        String cancelUrl = serverUrl + "/api/payment/cancel";

        ItemData item = ItemData.builder()
                .name(productName).quantity(1).price(price).build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode).amount(price).description(description)
                .returnUrl(returnUrl).cancelUrl(cancelUrl).item(item).build();

        CheckoutResponseData data = payOS.createPaymentLink(paymentData);
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
    public List<PaymentTransaction> getAllTransactions() {
        return paymentTransactionRepository.findAll();
    }

    @Override
    public List<PaymentTransaction> getTransactionsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return paymentTransactionRepository.findByUser(user);
    }

    @Override
    public void handleWebhook(String webhookBody) throws Exception {
        Map<String, Object> webhookData = objectMapper.readValue(webhookBody, new TypeReference<>() {});
        String signature = (String) webhookData.get("signature");
        if (signature == null || !verifySignature(webhookData, signature)) {
            log.error("Webhook verification failed!");
            throw new SecurityException("Invalid PayOS webhook signature");
        }
        String code = (String) webhookData.get("code");
        if (!"00".equals(code)) {
            log.info("Ignoring webhook event: {}", code);
            return;
        }
        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        String orderCode = String.valueOf(data.get("orderCode"));
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found for order code: " + orderCode));
        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS) {
            log.warn("Transaction {} already processed.", orderCode);
            return;
        }
        try {
            PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(Long.parseLong(orderCode));
            if ("PAID".equals(paymentLinkData.getStatus())) {
                transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
                paymentTransactionRepository.save(transaction);
                Account account = transaction.getUser().getAccount();
                LocalDateTime now = LocalDateTime.now();
                if (account.getRole() == Role.VIP && account.getVipEndDate() != null && account.getVipEndDate().isAfter(now)) {
                    account.setVipEndDate(account.getVipEndDate().plusDays(30));
                } else {
                    account.setRole(Role.VIP);
                    account.setVipStartDate(now);
                    account.setVipEndDate(now.plusDays(30));
                }
                accountRepository.save(account);
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

    private boolean verifySignature(Map<String, Object> webhookData, String receivedSignature) throws Exception {
        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        if (data == null) {
            return false;
        }
        List<String> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);
        StringBuilder dataToSign = new StringBuilder();
        for (String key : sortedKeys) {
            Object value = data.get(key);
            if (value != null) {
                if (dataToSign.length() > 0) {
                    dataToSign.append("&");
                }
                dataToSign.append(key).append("=").append(value.toString());
            }
        }
        String expectedSignature = createHmac(dataToSign.toString(), this.checksumKey);
        return expectedSignature.equals(receivedSignature);
    }

    private String createHmac(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKey);
        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}