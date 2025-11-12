package com.nhoclahola.socialnetworkv1.dto.message.response;

import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse
{
    private String messageId;
    private String content;
    private String imageUrl;
    private LocalDateTime timestamp;
    private UserResponse user;
}
