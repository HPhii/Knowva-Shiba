package com.example.demo.controller;

import com.example.demo.model.io.request.CreatePaymentRequest;
import com.example.demo.model.io.response.object.PaymentResponse;
import com.example.demo.service.intface.IPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class PaymentController {
    private final IPaymentService paymentService;

    @PostMapping("/create-payment-link")
    public ResponseEntity<PaymentResponse> createPaymentLink(@RequestBody CreatePaymentRequest request,
                                                             @RequestParam(defaultValue = "false") boolean isRenewal) throws Exception {
        PaymentResponse response = paymentService.createPaymentLink(request.getUserId(), isRenewal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public ResponseEntity<String> handleSuccess(@RequestParam String orderCode) throws Exception {
        paymentService.handlePaymentSuccess(orderCode);
        return ResponseEntity.ok("Payment successful");
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> handleCancel(@RequestParam String orderCode) throws Exception {
        paymentService.handlePaymentCancel(orderCode);
        return ResponseEntity.ok("Payment cancelled");
    }
}
