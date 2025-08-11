package com.example.demo.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.service.intface.ICloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService implements ICloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public Map upload(MultipartFile file) {
        try {
            // Gọi phương thức upload mới với byte array
            return this.upload(file.getBytes(), Map.of());
        } catch (IOException io) {
            // Gói exception gốc vào để không mất thông tin lỗi
            throw new RuntimeException("Image upload fail", io);
        }
    }

    // Implement phương thức mới
    @Override
    public Map upload(byte[] fileBytes, Map options) throws IOException {
        return this.cloudinary.uploader().upload(fileBytes, options);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // Sử dụng lại phương thức upload mới
            Map uploadResult = this.upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }
}