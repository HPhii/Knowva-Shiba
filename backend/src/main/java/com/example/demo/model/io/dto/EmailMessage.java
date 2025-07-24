package com.example.demo.model.io.dto;

import com.example.demo.model.io.response.object.EmailDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private EmailDetails emailDetails;
    private String templateName;
    private Map<String, Object> contextVariables;
}