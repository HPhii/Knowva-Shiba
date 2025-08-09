package com.example.demo.mapper;

import com.example.demo.model.entity.BlogCategory;
import com.example.demo.model.entity.BlogPost;
import com.example.demo.model.io.response.object.BlogCategoryResponse;
import com.example.demo.model.io.response.object.BlogPostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BlogMapper {

    @Mapping(target = "postCount", expression = "java(blogCategory.getPosts() != null ? (long) blogCategory.getPosts().size() : 0L)") // Thêm dòng này
    BlogCategoryResponse toBlogCategoryResponse(BlogCategory blogCategory);

    List<BlogCategoryResponse> toBlogCategoryResponseList(List<BlogCategory> blogCategories);

    @Mapping(source = "author.fullName", target = "authorName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "status", target = "status")
    BlogPostResponse toBlogPostResponse(BlogPost blogPost);

    List<BlogPostResponse> toBlogPostResponseList(List<BlogPost> blogPosts);
}