package com.example.demo.repository;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.QuizAccessControl;
import com.example.demo.model.entity.quiz.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizAccessControlRepository extends JpaRepository<QuizAccessControl, Long> {
    Optional<QuizAccessControl> findByQuizSetAndInvitedUser(QuizSet quizSet, User invitedUser);
}
