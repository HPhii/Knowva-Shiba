package com.example.demo.mapper;

import com.example.demo.model.entity.Feedback;
import com.example.demo.model.io.response.object.FeedbackResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.account.username", target = "username")
    FeedbackResponse toFeedbackResponse(Feedback feedback);

    List<FeedbackResponse> toFeedbackResponseList(List<Feedback> feedbacks);
}