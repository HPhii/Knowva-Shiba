package com.example.demo.model.io.request;

import com.example.demo.model.enums.BlogPostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBlogPostStatusRequest {
    @NotNull(message = "Status cannot be null")
    private BlogPostStatus status;
}
