package com.nhoclahola.socialnetworkv1.mapper;

import com.nhoclahola.socialnetworkv1.dto.notification.response.NotificationResponse;
import com.nhoclahola.socialnetworkv1.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface NotificationMapper
{
    @Mapping(source = "read", target = "isRead")
    public abstract NotificationResponse toNotificationResponse(Notification notification);

    public abstract List<NotificationResponse> toNotificationResponseList(List<Notification> notificationList);
}
