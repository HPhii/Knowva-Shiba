package com.example.demo.repository;

import com.example.demo.model.entity.Rating;
import com.example.demo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Projection Interface cho Rating Distribution
    interface RatingCount {
        Integer getRatingValue();
        Long getCount();
    }

    // JPA Query Methods - tìm kiếm rating của user cho từng loại entity
    Optional<Rating> findByUserAndQuizSetId(User user, Long quizSetId);
    Optional<Rating> findByUserAndFlashcardSetId(User user, Long flashcardSetId);
    Optional<Rating> findByUserAndBlogPostId(User user, Long blogPostId);

    // JPA Query Methods - đếm số lượng rating cho từng loại entity
    Long countByQuizSetId(Long quizSetId);
    Long countByFlashcardSetId(Long flashcardSetId);
    Long countByBlogPostId(Long blogPostId);

    // Giữ lại @Query cho các phương thức tính toán AVG (phức tạp hơn, rõ ràng hơn với JPQL)
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.quizSet.id = :quizSetId")
    Double getAverageRatingForQuizSet(@Param("quizSetId") Long quizSetId);

    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.flashcardSet.id = :flashcardSetId")
    Double getAverageRatingForFlashcardSet(@Param("flashcardSetId") Long flashcardSetId);

    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.blogPost.id = :blogPostId")
    Double getAverageRatingForBlogPost(@Param("blogPostId") Long blogPostId);

    // Thêm các phương thức truy vấn phân phối rating với JPQL
    @Query("SELECT r.ratingValue as ratingValue, COUNT(r) as count FROM Rating r WHERE r.quizSet.id = :entityId GROUP BY r.ratingValue")
    List<RatingCount> getRatingDistributionForQuizSet(@Param("entityId") Long entityId);

    @Query("SELECT r.ratingValue as ratingValue, COUNT(r) as count FROM Rating r WHERE r.flashcardSet.id = :entityId GROUP BY r.ratingValue")
    List<RatingCount> getRatingDistributionForFlashcardSet(@Param("entityId") Long entityId);

    @Query("SELECT r.ratingValue as ratingValue, COUNT(r) as count FROM Rating r WHERE r.blogPost.id = :entityId GROUP BY r.ratingValue")
    List<RatingCount> getRatingDistributionForBlogPost(@Param("entityId") Long entityId);
}
