package com.example.demo.model.io.response.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogCategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private long postCount;
}