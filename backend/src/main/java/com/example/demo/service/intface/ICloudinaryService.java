package com.example.demo.service.intface;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ICloudinaryService {
    Map upload(MultipartFile file);
    String uploadImage(MultipartFile file);
}

