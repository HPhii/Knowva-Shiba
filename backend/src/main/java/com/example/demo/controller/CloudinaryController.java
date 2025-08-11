package com.example.demo.controller;

import com.example.demo.service.impl.AsyncCloudinaryService;
import com.example.demo.service.intface.IAccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException; // Thêm import này
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class CloudinaryController {
    private final AsyncCloudinaryService asyncCloudinaryService;
    private final IAccountService accountService;

    @PostMapping("/cloudinary/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            Long userId = accountService.getCurrentAccount().getUser().getId();

            // === THAY ĐỔI QUAN TRỌNG Ở ĐÂY ===
            // 1. Đọc nội dung file ra mảng byte ngay lập tức
            byte[] fileBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();

            // 2. Truyền mảng byte và tên file vào phương thức async
            asyncCloudinaryService.uploadImageAndNotify(fileBytes, originalFilename, userId);
            // ===================================

            // Trả về response ngay lập tức
            return new ResponseEntity<>(Map.of("message", "Upload request received and is being processed."), HttpStatus.ACCEPTED);
        } catch (IOException e) { // Bắt IOException cụ thể hơn
            // Lỗi này xảy ra nếu không thể đọc file ngay từ đầu
            return new ResponseEntity<>(Map.of("error", "Failed to read file."), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "An unexpected error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}