package com.example.demo.model.entity.quiz;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.Rating;
import com.example.demo.model.entity.Comment;
import com.example.demo.model.enums.Category;
import com.example.demo.model.enums.SourceType;
import com.example.demo.model.enums.Visibility;
import com.example.demo.model.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private String title;
    
    private String description;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    private String language;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    private Integer maxQuestions;

    @Column(unique = true)
    private String accessToken;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;
    
    @Enumerated(EnumType.STRING)
    private Category category;

    private Integer timeLimit;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "quizSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<QuizQuestion> questions;

    @OneToMany(mappedBy = "quizSet", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<QuizAttempt> attempts;

    @OneToMany(mappedBy = "quizSet", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<QuizAccessControl> accessControlList;

    @OneToMany(mappedBy = "quizSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Rating> ratings;

    @OneToMany(mappedBy = "quizSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments;
}
