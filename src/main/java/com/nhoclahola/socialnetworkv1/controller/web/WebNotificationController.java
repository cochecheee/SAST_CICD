package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebNotificationController
{
    @GetMapping("/notifications")
    public String notificationPage()
    {
        return "user/notification_page";
    }
}
