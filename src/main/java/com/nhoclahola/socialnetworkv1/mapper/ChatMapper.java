package com.nhoclahola.socialnetworkv1.mapper;

import com.nhoclahola.socialnetworkv1.dto.chat.response.ChatResponse;
import com.nhoclahola.socialnetworkv1.entity.Chat;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ChatMapper
{
    public abstract ChatResponse toChatResponse(Chat chat);

    public abstract List<ChatResponse> toListChatResponse(List<Chat> chats);
}
