package com.nhoclahola.socialnetworkv1.mapper;

import com.nhoclahola.socialnetworkv1.dto.message.request.MessageCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.message.response.MessageResponse;
import com.nhoclahola.socialnetworkv1.entity.Message;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface MessageMapper
{
    public abstract Message messageCreateRequestToMessage(MessageCreateRequest request);

    public abstract MessageResponse toMessageResponse(Message message);

    public abstract List<MessageResponse> toListMessageResponse(List<Message> messages);
}
