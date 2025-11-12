package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.chat.response.ChatResponse;
import com.nhoclahola.socialnetworkv1.entity.Chat;

import java.util.List;

public interface ChatService
{
    public ChatResponse createChat(String targetUserId);

    public Chat findChatById(String chatId);

    public ChatResponse findChatByIdResponse(String chatId);

    public List<ChatResponse> findUsersChat();
}
