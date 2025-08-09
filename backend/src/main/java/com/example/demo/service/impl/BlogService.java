package com.example.demo.service.impl;

import com.example.demo.exception.DuplicateEntity;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.mapper.BlogMapper;
import com.example.demo.model.entity.BlogCategory;
import com.example.demo.model.entity.BlogPost;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.BlogPostStatus;
import com.example.demo.model.enums.Role;
import com.example.demo.model.io.request.CreateBlogCategoryRequest;
import com.example.demo.model.io.request.CreateBlogPostRequest;
import com.example.demo.model.io.request.UpdateBlogCategoryRequest;
import com.example.demo.model.io.request.UpdateBlogPostRequest;
import com.example.demo.model.io.response.object.BlogCategoryResponse;
import com.example.demo.model.io.response.object.BlogPostResponse;
import com.example.demo.model.io.response.paged.PagedBlogPostResponse;
import com.example.demo.repository.BlogCategoryRepository;
import com.example.demo.repository.BlogPostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IBlogService;
import com.example.demo.specification.BlogPostSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BlogService implements IBlogService {

    private final BlogPostRepository blogPostRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final UserRepository userRepository;
    private final BlogMapper blogMapper;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    // Constants for read time calculation
    private static final int AVERAGE_WORDS_PER_MINUTE = 200; // Average reading speed
    private static final int EXCERPT_WORD_LIMIT = 30; // Number of words for auto-generated excerpt

    @Override
    public PagedBlogPostResponse getAllBlogPosts(BlogPostStatus status, Long categoryId, Long authorId, String keyword, Pageable pageable) {
        Specification<BlogPost> spec = Specification.where(BlogPostSpecification.hasStatus(status))
                .and(BlogPostSpecification.hasCategory(categoryId))
                .and(BlogPostSpecification.hasAuthor(authorId))
                .and(BlogPostSpecification.titleContains(keyword));

        Page<BlogPost> blogPostPage = blogPostRepository.findAll(spec, pageable);

        return new PagedBlogPostResponse(
                blogMapper.toBlogPostResponseList(blogPostPage.getContent()),
                blogPostPage.getTotalElements(),
                blogPostPage.getTotalPages(),
                blogPostPage.getNumber()
        );
    }

    @Override
    public BlogPostResponse getBlogPostById(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog post not found with id: " + id));
        return blogMapper.toBlogPostResponse(blogPost);
    }

    @Override
    public BlogPostResponse getBlogPostBySlug(String slug) {
        BlogPost blogPost = blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Blog post not found with slug: " + slug));
        return blogMapper.toBlogPostResponse(blogPost);
    }

    @Override
    @Transactional
    public BlogPostResponse createBlogPost(CreateBlogPostRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + authorId));

        BlogCategory category = blogCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Blog category not found with id: " + request.getCategoryId()));

        String baseSlug = toSlug(request.getTitle());
        String slug = baseSlug;
        int counter = 1;
        while (blogPostRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        BlogPostStatus initialStatus = request.getStatus() == BlogPostStatus.DRAFT
                ? BlogPostStatus.DRAFT
                : BlogPostStatus.PENDING_APPROVAL;

        // Auto-generate excerpt and read time
        String excerpt = generateExcerpt(request.getContent(), request.getExcerpt());
        String readTime = calculateReadTime(request.getContent());

        BlogPost blogPost = BlogPost.builder()
                .title(request.getTitle())
                .slug(slug)
                .excerpt(excerpt)
                .content(request.getContent())
                .author(author)
                .category(category)
                .status(initialStatus)
                .imageUrl(request.getImageUrl())
                .readTime(readTime)
                .build();

        BlogPost savedBlogPost = blogPostRepository.save(blogPost);
        return blogMapper.toBlogPostResponse(savedBlogPost);
    }

    @Override
    @Transactional
    public BlogPostResponse updateBlogPost(Long postId, UpdateBlogPostRequest request, Long userId) {
        BlogPost blogPost = blogPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Blog post not found with id: " + postId));

        if (!Objects.equals(blogPost.getAuthor().getId(), userId)) {
            throw new ForbiddenException("You do not have permission to edit this post.");
        }

        // Update fields if provided
        if (StringUtils.hasText(request.getTitle())) {
            blogPost.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            blogPost.setContent(request.getContent());
        }
        if (request.getCategoryId() != null) {
            BlogCategory category = blogCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Blog category not found with id: " + request.getCategoryId()));
            blogPost.setCategory(category);
        }
        if (StringUtils.hasText(request.getImageUrl())) {
            blogPost.setImageUrl(request.getImageUrl());
        }

        if (request.getStatus() != null && blogPost.getStatus() == BlogPostStatus.DRAFT) {
            if (request.getStatus() == BlogPostStatus.PENDING_APPROVAL) {
                blogPost.setStatus(BlogPostStatus.PENDING_APPROVAL);
            }
        }

        // Auto-generate excerpt and read time based on current content
        String excerpt = generateExcerpt(blogPost.getContent(), request.getExcerpt());
        String readTime = calculateReadTime(blogPost.getContent());

        blogPost.setExcerpt(excerpt);
        blogPost.setReadTime(readTime);

        BlogPost updatedPost = blogPostRepository.save(blogPost);
        return blogMapper.toBlogPostResponse(updatedPost);
    }

    @Override
    public PagedBlogPostResponse getMyBlogPosts(Long authorId, BlogPostStatus status, Pageable pageable) {
        Specification<BlogPost> spec = Specification.where(BlogPostSpecification.hasAuthor(authorId));
        if (status != null) {
            spec = spec.and(BlogPostSpecification.hasStatus(status));
        }

        Page<BlogPost> blogPostPage = blogPostRepository.findAll(spec, pageable);

        return new PagedBlogPostResponse(
                blogMapper.toBlogPostResponseList(blogPostPage.getContent()),
                blogPostPage.getTotalElements(),
                blogPostPage.getTotalPages(),
                blogPostPage.getNumber()
        );
    }

    @Override
    @Transactional
    public void deleteBlogPost(Long id, Long userId) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog post not found with id: " + id));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Chỉ tác giả hoặc ADMIN mới được xóa
        if (!Objects.equals(blogPost.getAuthor().getId(), userId) && user.getAccount().getRole() != Role.ADMIN) {
            throw new ForbiddenException("You do not have permission to delete this post.");
        }

        blogPostRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BlogPostResponse updateBlogPostStatus(Long id, BlogPostStatus status) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog post not found with id: " + id));
        blogPost.setStatus(status);
        if (status == BlogPostStatus.PUBLISHED) {
            blogPost.setPublishedAt(LocalDateTime.now());
        }
        BlogPost savedBlogPost = blogPostRepository.save(blogPost);
        return blogMapper.toBlogPostResponse(savedBlogPost);
    }

    // --- Category Service Implementation ---

    @Override
    public List<BlogCategoryResponse> getAllBlogCategories() {
        return blogMapper.toBlogCategoryResponseList(blogCategoryRepository.findAll());
    }
    @Override
    @Transactional
    public BlogCategoryResponse createBlogCategory(CreateBlogCategoryRequest request) {
        String slug = toSlug(request.getName());
        if (blogCategoryRepository.existsBySlug(slug)) {
            throw new DuplicateEntity("A category with this name already exists.");
        }
        BlogCategory newCategory = BlogCategory.builder()
                .name(request.getName())
                .slug(slug)
                .build();
        BlogCategory savedCategory = blogCategoryRepository.save(newCategory);
        return blogMapper.toBlogCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public BlogCategoryResponse updateBlogCategory(Long categoryId, UpdateBlogCategoryRequest request) {
        BlogCategory category = blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        String newName = request.getName();
        String newSlug = toSlug(newName);

        if (!category.getSlug().equals(newSlug) && blogCategoryRepository.existsBySlug(newSlug)) {
            throw new DuplicateEntity("A category with this name already exists.");
        }

        category.setName(newName);
        category.setSlug(newSlug);
        BlogCategory updatedCategory = blogCategoryRepository.save(category);
        return blogMapper.toBlogCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteBlogCategory(Long categoryId) {
        BlogCategory category = blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        if (category.getPosts() != null && !category.getPosts().isEmpty()) {
            throw new IllegalStateException("Cannot delete a category that has posts associated with it.");
        }
        blogCategoryRepository.deleteById(categoryId);
    }

    private String toSlug(String input) {
        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Auto-generate excerpt from content if not provided
     */
    private String generateExcerpt(String content, String providedExcerpt) {
        // If user provided excerpt, use it
        if (StringUtils.hasText(providedExcerpt)) {
            return providedExcerpt.trim();
        }

        // Auto-generate from content
        if (!StringUtils.hasText(content)) {
            return "";
        }

        // Remove HTML tags and get plain text
        String plainText = content.replaceAll("<[^>]*>", "").trim();

        // Split into words and take first EXCERPT_WORD_LIMIT words
        String[] words = plainText.split("\\s+");
        if (words.length <= EXCERPT_WORD_LIMIT) {
            return plainText;
        }

        StringBuilder excerpt = new StringBuilder();
        for (int i = 0; i < EXCERPT_WORD_LIMIT; i++) {
            excerpt.append(words[i]).append(" ");
        }

        return excerpt.toString().trim() + "...";
    }

    /**
     * Calculate estimated read time based on word count
     */
    private String calculateReadTime(String content) {
        if (!StringUtils.hasText(content)) {
            return "1 min";
        }

        // Remove HTML tags and get plain text
        String plainText = content.replaceAll("<[^>]*>", "").trim();

        // Count words
        String[] words = plainText.split("\\s+");
        int wordCount = words.length;

        // Calculate minutes (minimum 1 minute)
        int minutes = Math.max(1, Math.round((float) wordCount / AVERAGE_WORDS_PER_MINUTE));

        if (minutes == 1) {
            return "1 min";
        } else if (minutes < 60) {
            return minutes + " mins";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours == 1 ? "1 hour" : hours + " hours";
            } else {
                return hours + "h " + remainingMinutes + "m";
            }
        }
    }
}
