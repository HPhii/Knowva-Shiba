package com.example.demo.service.strategy;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.BlogPost;
import com.example.demo.model.entity.Comment;
import com.example.demo.model.entity.Rating;
import com.example.demo.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlogPostInteractionStrategy implements EntityInteractionStrategy {

    private final BlogPostRepository blogPostRepository;

    @Override
    public String getEntityType() {
        return "blogpost";
    }

    @Override
    public void setEntityForRating(Rating rating, Long entityId) {
        BlogPost blogPost = findEntityById(entityId);
        rating.setBlogPost(blogPost);
    }

    @Override
    public void setEntityForComment(Comment comment, Long entityId) {
        BlogPost blogPost = findEntityById(entityId);
        comment.setBlogPost(blogPost);
    }

    @Override
    public BlogPost findEntityById(Long entityId) {
        return blogPostRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("BlogPost not found with id: " + entityId));
    }
}
