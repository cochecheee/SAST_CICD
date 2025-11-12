package com.nhoclahola.socialnetworkv1.dto.chat;

import com.nhoclahola.socialnetworkv1.entity.Message;
import com.nhoclahola.socialnetworkv1.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatWithLatestMessage
{
    private String chatId;
    private String chatName;
    private String chatImageUrl;
    private LocalDateTime timestamp;
    private Set<User> users;
    private Message latestMessage;
}
