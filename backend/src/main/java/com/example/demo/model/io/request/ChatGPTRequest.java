package com.example.demo.model.io.request;

import java.util.List;

public record ChatGPTRequest(String model, List<Message> messages) {
    public static record Message (String role, String content){}
}
