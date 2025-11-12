package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.notification.response.NotificationResponse;
import com.nhoclahola.socialnetworkv1.entity.Notification;
import com.nhoclahola.socialnetworkv1.entity.NotificationType;
import com.nhoclahola.socialnetworkv1.entity.Post;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.mapper.NotificationMapper;
import com.nhoclahola.socialnetworkv1.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImplementation implements NotificationService
{
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    private final SimpMessagingTemplate simpMessagingTemplate;


    @Override
    public Notification findNotificationById(String notificationId)
    {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXIST));
    }

    @Override
    @Async
    @Transactional
    public void createNotification(NotificationType notificationType, User user, User triggerUser, Post post)
    {
        Notification notification = Notification.builder()
                .notificationType(notificationType)
                .user(user)
                .triggerUser(triggerUser)
                .post(post)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toNotificationResponse(notification);
        simpMessagingTemplate.convertAndSendToUser(post.getUser().getUserId(), "/notification/private", response);
    }

    @Override
    public String markAsRead(String notificationId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Notification notification = this.findNotificationById(notificationId);
        if (!currentUserEmail.equals(notification.getUser().getEmail()))
            throw new AppException(ErrorCode.NOT_YOUR_NOTIFICATION);
        if (!notification.isRead())
        {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        return "read";
    }

    @Override
    public List<NotificationResponse> findNotificationsByAuth(int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        List<Notification> notifications = notificationRepository.findNotificationsByEmail(currentUserEmail, pageable);
        return notificationMapper.toNotificationResponseList(notifications);
    }

    @Override
    public int countNotReadNotificationsByAuth()
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificationRepository.countNotReadNotificationsByEmail(currentUserEmail);
    }
}
