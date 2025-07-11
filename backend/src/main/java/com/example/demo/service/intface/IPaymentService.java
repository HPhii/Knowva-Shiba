package com.example.demo.service.intface;

import com.example.demo.model.entity.PaymentTransaction;
import com.example.demo.model.io.response.object.PaymentResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface IPaymentService {
    PaymentResponse createPaymentLink(Long userId, boolean isRenewal) throws Exception;
    List<PaymentTransaction> getAllTransactions();
    List<PaymentTransaction> getTransactionsByUserId(Long userId);
    void handleWebhook(String webhookBody) throws Exception;
    void handleCancel(String orderCode);
}