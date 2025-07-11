package com.example.demo.controller;

import com.example.demo.model.io.request.CreatePaymentRequest;
import com.example.demo.model.io.response.object.PaymentResponse;
import com.example.demo.service.intface.IPaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "4. Payment Management")
public class PaymentController {
    private final IPaymentService paymentService;

    @Value("${client.url}")
    private String clientUrl;

    @PostMapping("/create-payment-link")
    @SecurityRequirement(name = "api")
    @Operation(summary = "Tạo link thanh toán VIP", description = "Tạo một link thanh toán PayOS để nâng cấp hoặc gia hạn VIP. Yêu cầu xác thực.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo link thành công, trả về URL thanh toán"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @ApiResponse(responseCode = "500", description = "Lỗi khi tạo link thanh toán")
    })
    public ResponseEntity<PaymentResponse> createPaymentLink(
            @RequestBody CreatePaymentRequest request,
            @Parameter(description = "Đánh dấu nếu đây là gia hạn VIP. Mặc định là false (nâng cấp).")
            @RequestParam(defaultValue = "false") boolean isRenewal) throws Exception {
        PaymentResponse response = paymentService.createPaymentLink(request.getUserId(), isRenewal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    @Hidden // Ẩn API này khỏi Swagger UI vì đây là URL callback, không dành cho người dùng gọi trực tiếp.
    public void handleSuccessRedirect(HttpServletResponse response, @RequestParam String orderCode, @RequestParam String status) throws IOException {
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-success")
                .queryParam("orderCode", orderCode)
                .queryParam("status", status)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/cancel")
    @Hidden // Ẩn API này khỏi Swagger UI
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
    @Hidden // Ẩn API này khỏi Swagger UI vì đây là webhook do PayOS gọi.
    public ResponseEntity<String> payosWebhook(@RequestBody String webhookBody) {
        try {
            paymentService.handleWebhook(webhookBody);
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook processing failed: " + e.getMessage());
        }
    }
}