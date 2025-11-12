package com.nhoclahola.socialnetworkv1.controller.api;

import com.nhoclahola.socialnetworkv1.dto.ApiResponse;
import com.nhoclahola.socialnetworkv1.dto.message.request.MessageCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.message.response.MessageResponse;
import com.nhoclahola.socialnetworkv1.service.MessageService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MessageController
{
    private final MessageService messageService;

//    @PostMapping("/messages/chats/{chatId}")
//    public ApiResponse<MessageResponse> createMessage(@PathVariable String chatId,
//                                         @RequestBody @Valid MessageCreateRequest request)
//    {
//        MessageResponse messageResponse = messageService.createMessage(chatId, request);
//        ApiResponse<MessageResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(messageResponse);
//        return apiResponse;
//    }

    @GetMapping("/messages/chats/{chatId}")
    public ApiResponse<List<MessageResponse>> getChatsMessages(@PathVariable String chatId, @RequestParam int index)
    {
        List<MessageResponse> messageResponseList = messageService.findMessagesByChatId(chatId, index);
        ApiResponse<List<MessageResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(messageResponseList);
        return apiResponse;
    }

    @GetMapping("/messages/{messageId}")
    public MessageResponse getMessageById(@PathVariable String messageId)
    {
        return messageService.findMessageByIdResponse(messageId);
    }
}
