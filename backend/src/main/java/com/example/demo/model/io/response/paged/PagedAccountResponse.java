package com.example.demo.model.io.response.paged;

import com.example.demo.model.io.response.object.AccountResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedAccountResponse {
    private List<AccountResponse> accounts;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}