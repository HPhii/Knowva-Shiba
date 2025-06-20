package com.example.demo.model.io.request.flashcard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveFlashcardRequest {
    private String front;
    private String back;
    private String imageUrl;
    private Integer order;
}
