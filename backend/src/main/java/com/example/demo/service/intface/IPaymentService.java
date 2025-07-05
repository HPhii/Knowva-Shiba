package com.example.demo.service.intface;

import com.example.demo.model.entity.PaymentTransaction;
import com.example.demo.model.io.response.object.PaymentResponse;

import java.util.List;

public interface IPaymentService {
    PaymentResponse createPaymentLink(Long userId, boolean isRenewal) throws Exception;
    void handlePaymentSuccess(String orderCode) throws Exception;
    void handlePaymentCancel(String orderCode) throws Exception;
    List<PaymentTransaction> getAllTransactions();
    List<PaymentTransaction> getTransactionsByUserId(Long userId);
}
