package com.nhoclahola.socialnetworkv1.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebAdminController
{
    @GetMapping("/admin/dashboard")
    public String dashboard()
    {
        return "admin/admin_dashboard";
    }

    @GetMapping("/admin/manage_user")
    public String manageUser()
    {
        return "admin/manage_user";
    }

    @GetMapping("/admin/edit_user/{userId}")
    public String editUser(@PathVariable String userId)
    {
        return "admin/form_edit";
    }

    @GetMapping("/admin/manage_post")
    public String managePost()
    {
        return "admin/manage_post";
    }
}
