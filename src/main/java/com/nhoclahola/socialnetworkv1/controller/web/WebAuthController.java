package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebAuthController
{
    @GetMapping("/login")
    public String login()
    {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register()
    {
        return "auth/register";
    }

    @GetMapping("/reset-password")
    public String resetPassword()
    {
        return "auth/reset_password";
    }
}
