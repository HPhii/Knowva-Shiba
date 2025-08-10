package com.example.demo.utils;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class ByteArrayMultipartFile implements MultipartFile {
    private final byte[] content;
    private final String name;
    private final String contentType;

    public ByteArrayMultipartFile(byte[] content, String name, String contentType) {
        this.content = content;
        this.name = name;
        this.contentType = contentType;
    }

    @Override
    public String getName() { return name; }
    @Override
    public String getOriginalFilename() { return name; }
    @Override
    public String getContentType() { return contentType; }
    @Override
    public boolean isEmpty() { return content == null || content.length == 0; }
    @Override
    public long getSize() { return content.length; }
    @Override
    public byte[] getBytes() { return content; }
    @Override
    public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(content); }
    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
            fos.write(content);
        }
    }
    @Override
    public Resource getResource() { return new ByteArrayResource(content, name); }
}
