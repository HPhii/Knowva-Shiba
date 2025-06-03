package com.example.demo.model.entity.flashcard;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_access_control")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardAccessControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashcardSet flashcardSet;

    @ManyToOne
    @JoinColumn(name = "invited_user_id", nullable = false)
    private User invitedUser;

    @Enumerated(EnumType.STRING)
    private Permission permission;

    private LocalDateTime invitedAt;
}
