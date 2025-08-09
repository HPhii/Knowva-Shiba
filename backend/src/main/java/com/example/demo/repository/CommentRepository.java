package com.example.demo.repository;

import com.example.demo.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByQuizSetIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(Long quizSetId, Pageable pageable);
    Page<Comment> findByFlashcardSetIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(Long flashcardSetId, Pageable pageable);
    Page<Comment> findByBlogPostIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(Long blogPostId, Pageable pageable);

//    Page<Comment> findByQuizSet_IdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(Long quizSetId, Pageable pageable);
    // get reply comments for a specific comment
    List<Comment> findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentCommentId);

    // count methods
    Long countByQuizSetIdAndIsDeletedFalse(Long quizSetId);
    Long countByFlashcardSetIdAndIsDeletedFalse(Long flashcardSetId);
    Long countByBlogPostIdAndIsDeletedFalse(Long blogPostId);
}
