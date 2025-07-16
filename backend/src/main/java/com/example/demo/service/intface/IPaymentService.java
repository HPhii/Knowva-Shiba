package com.example.demo.service.intface;

import com.example.demo.model.entity.PaymentTransaction;
import com.example.demo.model.io.response.object.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IPaymentService {
    PaymentResponse createPaymentLink(Long userId, boolean isRenewal) throws Exception;

    Page<PaymentTransaction> getAllTransactions(
            Long userId, String username, String email, String orderCode,
            PaymentTransaction.TransactionStatus status,
            Pageable pageable);

    List<PaymentTransaction> getTransactionsByUserId(Long userId);
    void handleWebhook(String webhookBody) throws Exception;
    void handleCancel(String orderCode);
}