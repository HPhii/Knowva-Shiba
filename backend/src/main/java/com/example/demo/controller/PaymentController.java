package com.example.demo.controller;

import com.example.demo.model.io.request.CreatePaymentRequest;
import com.example.demo.model.io.response.object.PaymentResponse;
import com.example.demo.service.intface.IPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PaymentController {
    private final IPaymentService paymentService;

    @Value("${client.url}")
    private String clientUrl;

    @PostMapping("/create-payment-link")
    @SecurityRequirement(name = "api")
    public ResponseEntity<PaymentResponse> createPaymentLink(@RequestBody CreatePaymentRequest request,
                                                             @RequestParam(defaultValue = "false") boolean isRenewal) throws Exception {
        PaymentResponse response = paymentService.createPaymentLink(request.getUserId(), isRenewal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public void handleSuccessRedirect(HttpServletResponse response, @RequestParam String orderCode, @RequestParam String status) throws IOException {
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-success")
                .queryParam("orderCode", orderCode)
                .queryParam("status", status)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/cancel")
    public void handleCancelRedirect(HttpServletResponse response, @RequestParam String orderCode, @RequestParam String status) throws IOException {
        if ("CANCELLED".equalsIgnoreCase(status)) {
            paymentService.handleCancel(orderCode);
        }

        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-cancelled")
                .queryParam("orderCode", orderCode)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<String> payosWebhook(@RequestBody String webhookBody) {
        try {
            paymentService.handleWebhook(webhookBody);
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook processing failed: " + e.getMessage());
        }
    }
}