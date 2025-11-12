package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebProfileController
{
    @GetMapping("/profile/me")
    public String profileMe()
    {
        return "user/profile_me";
    }

    @GetMapping("/profile/{userId}")
    public String profile(@PathVariable String userId)
    {
        return "user/user_profile_page";
    }
}
