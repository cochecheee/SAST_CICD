package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.notification.response.NotificationResponse;
import com.nhoclahola.socialnetworkv1.entity.Notification;
import com.nhoclahola.socialnetworkv1.entity.NotificationType;
import com.nhoclahola.socialnetworkv1.entity.Post;
import com.nhoclahola.socialnetworkv1.entity.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationService
{
    public abstract Notification findNotificationById(String notificationId);

    public abstract void createNotification(NotificationType notificationType, User user, User triggerUser, Post post);

    public abstract String markAsRead(String notificationId);

    public abstract List<NotificationResponse> findNotificationsByAuth(int index);

    public abstract int countNotReadNotificationsByAuth();
}
