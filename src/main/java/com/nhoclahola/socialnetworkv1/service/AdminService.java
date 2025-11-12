package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.admin.response.DashBoardInfo;
import com.nhoclahola.socialnetworkv1.dto.post.response.PostResponse;
import com.nhoclahola.socialnetworkv1.dto.user.request.AdminResetPassword;
import com.nhoclahola.socialnetworkv1.dto.user.request.AdminUpdateUser;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface AdminService
{
    DashBoardInfo getDashBoardInfo();

    Page<UserResponse> getAllUsersAdmin(int page);

    UserResponse adminUpdateUser(String userId, AdminUpdateUser request);

    UserResponse adminUpdatePassword(String userId, AdminResetPassword request);

    Page<PostResponse> getAllPostsAdmin(int page);

    @Transactional
    String adminDeletePost(String postId);

    @Transactional
    String adminDeleteUser(String userId);
}
