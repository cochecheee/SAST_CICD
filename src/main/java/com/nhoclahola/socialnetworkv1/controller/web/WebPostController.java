package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebPostController
{
    @GetMapping("/posts/{postId}")
    public String profileMe(@PathVariable String postId)
    {
        return "user/post_page";
    }
}
