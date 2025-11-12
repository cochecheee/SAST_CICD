package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.message.request.MessageCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.message.response.MessageResponse;
import com.nhoclahola.socialnetworkv1.entity.Message;

import java.util.List;

public interface MessageService
{
    public abstract MessageResponse createMessage(String requestUserEmail, String chatId, MessageCreateRequest request);

    public abstract List<MessageResponse> findMessagesByChatId(String chatId, int index);

    public abstract Message findMessageById(String messageId);

    public abstract MessageResponse findMessageByIdResponse(String messageId);
}
