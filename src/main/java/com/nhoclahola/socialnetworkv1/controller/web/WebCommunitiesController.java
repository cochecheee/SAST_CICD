package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebCommunitiesController
{
    @GetMapping("/communities")
    public String communitiesPage()
    {
        return "user/communities_page";
    }
}
