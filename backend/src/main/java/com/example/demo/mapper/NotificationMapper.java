package com.example.demo.mapper;


import com.example.demo.model.entity.Notification;
import com.example.demo.model.io.response.object.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "setId", target = "setId")
    NotificationResponse toNotificationResponse(Notification notification);

    List<NotificationResponse> toNotificationResponseList(List<Notification> notifications);
}
