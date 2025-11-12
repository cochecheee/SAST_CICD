package com.nhoclahola.socialnetworkv1.controller.api;

import com.nhoclahola.socialnetworkv1.dto.ApiResponse;
import com.nhoclahola.socialnetworkv1.dto.notification.response.NotificationResponse;
import com.nhoclahola.socialnetworkv1.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationController
{
    private final NotificationService notificationService;

    @PutMapping("/notifications/{notificationId}/read")
    public ApiResponse<String> markNotificationAsRead(@PathVariable String notificationId)
    {
        String result = notificationService.markAsRead(notificationId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping("/notifications/users")
    public ApiResponse<List<NotificationResponse>> getUsersNotifications(@RequestParam int index)
    {
        List<NotificationResponse> notificationResponseList = notificationService.findNotificationsByAuth(index);
        ApiResponse<List<NotificationResponse>> response = new ApiResponse<>();
        response.setResult(notificationResponseList);
        return response;
    }

    @GetMapping("/notifications/count_not_read")
    public ApiResponse<Integer> getUsersNotReadNotificationsCount()
    {
        Integer count = notificationService.countNotReadNotificationsByAuth();
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setResult(count);
        return response;
    }
}
