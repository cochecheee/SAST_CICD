package com.nhoclahola.socialnetworkv1.controller.api;

import com.nhoclahola.socialnetworkv1.dto.ApiResponse;
import com.nhoclahola.socialnetworkv1.dto.chat.response.ChatResponse;
import com.nhoclahola.socialnetworkv1.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController
{
    private final ChatService chatService;

    @PostMapping("/chats/users/{targetUserId}")
    public ApiResponse<ChatResponse> createChat(@PathVariable String targetUserId)
    {
        ChatResponse chatResponse = chatService.createChat(targetUserId);
        ApiResponse<ChatResponse> response = new ApiResponse<>();
        response.setResult(chatResponse);
        return response;
    }

    @GetMapping("/chats/user-chat")
    public ApiResponse<List<ChatResponse>> getUsersChat()
    {
        List<ChatResponse> chatResponseList = chatService.findUsersChat();
        ApiResponse<List<ChatResponse>> response = new ApiResponse<>();
        response.setResult(chatResponseList);
        return response;
    }

    @GetMapping("/chats/{chatId}")
    public ApiResponse<ChatResponse> getChatById(@PathVariable String chatId)
    {
        ChatResponse chatResponse = chatService.findChatByIdResponse(chatId);
        ApiResponse<ChatResponse> response = new ApiResponse<>();
        response.setResult(chatResponse);
        return response;
    }
}
