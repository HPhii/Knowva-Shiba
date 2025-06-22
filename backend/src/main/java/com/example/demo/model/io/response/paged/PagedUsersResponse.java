package com.example.demo.model.io.response.paged;

import com.example.demo.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedUsersResponse {
    private List<User> users;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
