package com.example.demo.model.entity.flashcard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashcardSet flashcardSet;

    @Column(nullable = false)
    private String front;

    @Column(nullable = false)
    private String back;

    private String imageUrl;

    @Column(name = "`order`")
    private Integer order;

    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FlashcardProgress> progressList;
}
