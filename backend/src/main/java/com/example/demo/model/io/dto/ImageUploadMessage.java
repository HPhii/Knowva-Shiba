package com.example.demo.model.io.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadMessage {
    private byte[] fileContent;
    private String originalFilename;
    private Long userId; // Để biết trả kết quả về cho ai qua WebSocket
}