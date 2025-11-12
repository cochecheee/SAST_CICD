package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.chat.response.ChatResponse;
import com.nhoclahola.socialnetworkv1.entity.Chat;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.mapper.ChatMapper;
import com.nhoclahola.socialnetworkv1.repository.ChatRepository;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatServiceImplementation implements ChatService
{
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final ChatMapper chatMapper;

    @Override
    @Transactional
    public ChatResponse createChat(String targetUserId)
    {
        String reqUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User reqUser = userService.findUserByEmail(reqUserEmail);

        if (targetUserId.equals(reqUser.getUserId()))
            throw new RuntimeException("Can't create chat for just one person");

        User targetUser = userService.findUserById(targetUserId);
        // Already exist chat
        Chat isExist = chatRepository.findChatByUserId(reqUser, targetUser);
        if (isExist != null)
            return chatMapper.toChatResponse(isExist);
        // Not exist
        Set<User> users = new HashSet<>();
        users.add(reqUser);
        users.add(targetUser);
        Chat chat = Chat.builder()
                .users(users)
                .timestamp(LocalDateTime.now())
                .build();
        chatRepository.save(chat);
        return chatMapper.toChatResponse(chat);
    }

    @Override
    public Chat findChatById(String chatId)
    {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_NOT_EXIST));
    }

    @Override
    public ChatResponse findChatByIdResponse(String chatId)
    {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_NOT_EXIST));
        return chatMapper.toChatResponse(chat);
    }

    @Override
    public List<ChatResponse> findUsersChat()
    {
        String requestUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User requestUser = userService.findUserByEmail(requestUserEmail);
        List<Chat> chats = chatRepository.findByUsersId(requestUser.getUserId());
        return chatMapper.toListChatResponse(chats);
    }
}
