package com.example.demo.model.entity.flashcard;

import com.example.demo.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_spaced_repetition_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSpacedRepetitionSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer newFlashcardsPerDay;
}
