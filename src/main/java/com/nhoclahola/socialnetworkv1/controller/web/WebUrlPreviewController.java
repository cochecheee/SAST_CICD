package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebUrlPreviewController {

    @GetMapping("/url-preview")
    public String urlPreviewPage() {
        return "user/url_preview_page";
    }
}

