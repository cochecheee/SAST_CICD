package com.nhoclahola.socialnetworkv1.dto.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhoclahola.socialnetworkv1.dto.post.response.PostResponse;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import com.nhoclahola.socialnetworkv1.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse
{
    private String notificationId;
    private boolean isRead;
    // Redundant field because the post field already has it
//    private UserResponse user;
    private PostResponse post;
    private UserResponse triggerUser;
    private NotificationType notificationType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
