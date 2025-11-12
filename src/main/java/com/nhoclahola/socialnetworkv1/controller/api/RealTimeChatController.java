package com.nhoclahola.socialnetworkv1.controller.api;

import com.nhoclahola.socialnetworkv1.dto.message.request.MessageCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.message.response.MessageResponse;
import com.nhoclahola.socialnetworkv1.entity.Message;
import com.nhoclahola.socialnetworkv1.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@RequiredArgsConstructor
@Controller
public class RealTimeChatController
{
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat/{groupId}")
    public MessageResponse sendToUser(@Payload MessageCreateRequest message, @DestinationVariable String groupId, SimpMessageHeaderAccessor headerAccessor)
    {
        String userEmail = Objects.requireNonNull(headerAccessor.getUser()).getName();
        MessageResponse messageResponse = messageService.createMessage(userEmail, groupId, message);
        simpMessagingTemplate.convertAndSendToUser(groupId, "/chat/private", messageResponse);
        return messageResponse;
    }

    @MessageMapping("/notification/{userId}")
    public void sendNotificationToUser(@Payload String message, @DestinationVariable String userId)
    {
        System.out.println(message);
        simpMessagingTemplate.convertAndSendToUser(userId, "/notification/private", message);
    }
}
