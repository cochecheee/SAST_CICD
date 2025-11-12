package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.message.request.MessageCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.message.response.MessageResponse;
import com.nhoclahola.socialnetworkv1.entity.Chat;
import com.nhoclahola.socialnetworkv1.entity.Message;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.mapper.MessageMapper;
import com.nhoclahola.socialnetworkv1.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImplementation implements MessageService
{
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChatService chatService;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public MessageResponse createMessage(String requestUserEmail, String chatId, MessageCreateRequest request)
    {
        User user = userService.findUserByEmail(requestUserEmail);
        Chat chat = chatService.findChatById(chatId);
        if (!chat.getUsers().contains(user))
            throw new AppException(ErrorCode.USER_NOT_EXIST_IN_CHAT);
        Message message = messageMapper.messageCreateRequestToMessage(request);
        message.setUser(user);
        message.setChat(chat);
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);
        return messageMapper.toMessageResponse(message);
    }

    @Override
    public List<MessageResponse> findMessagesByChatId(String chatId, int index)
    {   String userRequestEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User requestUser = userService.findUserByEmail(userRequestEmail);
        Chat chat = chatService.findChatById(chatId);
        if (!chat.getUsers().contains(requestUser))
            throw new AppException(ErrorCode.USER_NOT_EXIST_IN_CHAT);
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("timestamp").descending());
        List<Message> messages = messageRepository.findMessagesByChatId(chatId, pageable);
        messages.sort(Comparator.comparing((message) -> message.getTimestamp()));
        return messageMapper.toListMessageResponse(messages);
    }

    @Override
    public Message findMessageById(String messageId)
    {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_EXIST));
    }

    @Override
    public MessageResponse findMessageByIdResponse(String messageId)
    {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_EXIST));
        return messageMapper.toMessageResponse(message);
    }
}
