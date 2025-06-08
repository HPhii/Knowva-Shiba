package com.example.demo.config;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FileSystemResourceValue extends ByteArrayResource {
    private final String filename;

    public FileSystemResourceValue(MultipartFile file) throws IOException {
        super(file.getBytes());
        this.filename = file.getOriginalFilename();
    }

    @Override
    public String getFilename() {
        return this.filename;
    }
}
