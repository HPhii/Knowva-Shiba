package com.example.demo.model.io.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyProgressStats {
    private int unstudiedCount;
    private int dontKnowCount;
    private int knowCount;
}
