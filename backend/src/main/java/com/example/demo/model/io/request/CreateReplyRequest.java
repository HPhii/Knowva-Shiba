package com.example.demo.model.io.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyRequest {
    @NotBlank(message = "Reply message cannot be blank")
    private String message;
    private Long parentId;
}