package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebMessageController
{
    @GetMapping("/messages")
    public String messages()
    {
        return "user/message_page";
    }
}
