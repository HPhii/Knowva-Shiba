package com.example.demo.service.intface;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException; // Thêm import
import java.util.Map;

public interface ICloudinaryService {
    Map upload(MultipartFile file);

    // Thêm phương thức mới để xử lý trực tiếp byte array
    Map upload(byte[] fileBytes, Map options) throws IOException;

    String uploadImage(MultipartFile file);
}