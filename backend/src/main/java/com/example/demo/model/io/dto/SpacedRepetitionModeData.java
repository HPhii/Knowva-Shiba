package com.example.demo.model.io.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpacedRepetitionModeData {
    private boolean firstTime;
    private String message;
    private Integer newFlashcardsPerDay;
    private Integer newCardsCount;
    private Integer knowCardsCount;
}
