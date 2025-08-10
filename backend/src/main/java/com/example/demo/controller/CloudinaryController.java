package com.example.demo.controller;

import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.kafka.CloudinaryProducerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class CloudinaryController {
    private final CloudinaryProducerService cloudinaryProducerService;
    private final IAccountService accountService;

    @PostMapping("/cloudinary/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            Long userId = accountService.getCurrentAccount().getUser().getId();
            cloudinaryProducerService.sendUploadRequest(file, userId);
            // Trả về response ngay lập tức
            return new ResponseEntity<>(Map.of("message", "Upload request received and is being processed."), HttpStatus.ACCEPTED);
        } catch (IOException e) {
            return new ResponseEntity<>(Map.of("error", "Failed to process file."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}