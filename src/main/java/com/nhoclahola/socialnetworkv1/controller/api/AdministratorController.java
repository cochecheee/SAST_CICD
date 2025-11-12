package com.nhoclahola.socialnetworkv1.controller.api;

import com.nhoclahola.socialnetworkv1.dto.ApiResponse;
import com.nhoclahola.socialnetworkv1.dto.admin.response.DashBoardInfo;
import com.nhoclahola.socialnetworkv1.dto.comment.response.CommentWithDataResponse;
import com.nhoclahola.socialnetworkv1.dto.post.response.PostResponse;
import com.nhoclahola.socialnetworkv1.dto.user.request.AdminResetPassword;
import com.nhoclahola.socialnetworkv1.dto.user.request.AdminUpdateUser;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import com.nhoclahola.socialnetworkv1.service.AdminService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdministratorController
{
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ApiResponse<DashBoardInfo> getDashBoardInfo()
    {
        DashBoardInfo dashBoardInfo = adminService.getDashBoardInfo();
        ApiResponse<DashBoardInfo> response = new ApiResponse<>();
        response.setResult(dashBoardInfo);
        return response;
    }

    @GetMapping("/users")
    public ApiResponse<Page<UserResponse>> getAllUsers(@RequestParam int page)
    {
        ApiResponse<Page<UserResponse>> response = new ApiResponse<>();
        response.setResult(adminService.getAllUsersAdmin(page));
        return response;
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<UserResponse> adminUpdateUser(@PathVariable String userId, @RequestBody @Valid AdminUpdateUser request)
    {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(adminService.adminUpdateUser(userId, request));
        return response;
    }

    @PutMapping("/users/{userId}/reset-password")
    public ApiResponse<UserResponse> adminUpdatePassword(@PathVariable String userId, @RequestBody @Valid AdminResetPassword request)
    {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(adminService.adminUpdatePassword(userId, request));
        return response;
    }

    @GetMapping("/posts")
    public ApiResponse<Page<PostResponse>> getAllPosts(@RequestParam int page)
    {
        ApiResponse<Page<PostResponse>> response = new ApiResponse<>();
        response.setResult(adminService.getAllPostsAdmin(page));
        return response;
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<String> adminDeletePost(@PathVariable String postId)
    {
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult(adminService.adminDeletePost(postId));
        return response;
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<String> adminDeleteUser(@PathVariable String userId)
    {
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult(adminService.adminDeleteUser(userId));
        return response;
    }
}
