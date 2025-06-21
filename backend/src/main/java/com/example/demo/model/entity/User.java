package com.example.demo.model.entity;

import com.example.demo.model.entity.flashcard.FlashcardAccessControl;
import com.example.demo.model.entity.flashcard.FlashcardProgress;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import com.example.demo.model.entity.quiz.QuizAccessControl;
import com.example.demo.model.entity.quiz.QuizAttempt;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Account account;

    // Optional: các mối quan hệ trong tương lai
    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    private List<FlashcardSet> flashcardSets;

    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    private List<QuizSet> quizSets;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<QuizAttempt> quizAttempts;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<FlashcardProgress> flashcardProgressList;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<FlashcardAttempt> flashcardAttempts;

    @OneToMany(mappedBy = "invitedUser")
    @JsonIgnore
    private List<QuizAccessControl> quizInvitations;

    @OneToMany(mappedBy = "invitedUser")
    @JsonIgnore
    private List<FlashcardAccessControl> flashcardInvitations;
}
