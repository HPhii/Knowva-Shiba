package com.example.demo.specification;

import com.example.demo.model.entity.BlogCategory;
import com.example.demo.model.entity.BlogPost;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.BlogPostStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class BlogPostSpecification {

    public static Specification<BlogPost> hasStatus(BlogPostStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<BlogPost> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<BlogPost, BlogCategory> categoryJoin = root.join("category");
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<BlogPost> hasAuthor(Long authorId) {
        return (root, query, criteriaBuilder) -> {
            if (authorId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<BlogPost, User> authorJoin = root.join("author");
            return criteriaBuilder.equal(authorJoin.get("id"), authorId);
        };
    }

    public static Specification<BlogPost> titleContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }
}