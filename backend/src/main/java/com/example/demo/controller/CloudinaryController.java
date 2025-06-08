package com.example.demo.controller;

import com.example.demo.service.intface.ICloudinaryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class CloudinaryController {
    private final ICloudinaryService cloudinaryService;

    @PostMapping("/cloudinary/upload")
    public ResponseEntity<Map> uploadImage(@RequestParam("image") MultipartFile file){
        Map data = this.cloudinaryService.upload(file);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
