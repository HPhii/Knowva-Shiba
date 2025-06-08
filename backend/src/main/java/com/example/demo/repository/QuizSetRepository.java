package com.example.demo.repository;

import com.example.demo.model.entity.quiz.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {
}
