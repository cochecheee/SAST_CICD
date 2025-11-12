package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.auth.request.UserCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.request.UserResetPasswordRequest;
import com.nhoclahola.socialnetworkv1.dto.user.request.UserUpdateRequest;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserWithDataResponse;
import com.nhoclahola.socialnetworkv1.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService
{
    public abstract List<UserResponse> findAllUsers();

    public abstract User createUser(UserCreateRequest request);

    User resetPassword(UserResetPasswordRequest request);

    public abstract User findUserById(String userId);

    public abstract UserResponse findUserByIdResponse(String userId);

    public abstract UserWithDataResponse findUserDataByUserId(String userId);

    public abstract User findUserByEmail(String email);

    UserWithDataResponse findCurrentUserData();

    public abstract String followUser(String userIdToFollow);

    public abstract UserResponse updateUser(String userId, UserUpdateRequest request);

    public abstract List<UserResponse> searchUser(String query, int index);

    public abstract UserResponse findUserFromToken();

    public abstract UserResponse updateUserFromToken(UserUpdateRequest request);

    public abstract List<UserResponse> findUsersFollowing(String userId, int index);

    public abstract List<UserResponse> findUsersFollower(String userId, int index);

    public abstract UserResponse uploadAvatar(MultipartFile image) throws IOException;

    public abstract UserResponse uploadCover(MultipartFile image) throws IOException;

    public abstract List<UserResponse> findSuggestedUsers();

    public abstract List<UserResponse> findLatestActivityUsersFollowings();

    public abstract List<UserResponse> findUsersLikedPostByPostId(String postId, int index);

    public abstract List<UserResponse> findUsersLikedCommentByCommentId(String commentId, int index);
}
