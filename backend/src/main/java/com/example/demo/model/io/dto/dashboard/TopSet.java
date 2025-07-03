package com.example.demo.model.io.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSet {
    private Long id;
    private String title;
    private long attemptCount;
}
