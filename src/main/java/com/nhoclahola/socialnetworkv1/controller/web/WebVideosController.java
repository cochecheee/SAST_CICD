package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebVideosController
{
    @GetMapping("/videos")
    public String popularVideos()
    {
        return "user/popular_videos";
    }
}
