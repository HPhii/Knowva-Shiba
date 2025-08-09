package com.example.demo.model.io.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBlogCategoryRequest {
    @NotBlank(message = "Category name cannot be blank")
    private String name;
}