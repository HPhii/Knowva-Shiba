package com.example.demo.model.io.response.object.flashcard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardResponse {
    private Long id;
    private String front;
    private String back;
    private String imageUrl;
    private Integer order;
}
