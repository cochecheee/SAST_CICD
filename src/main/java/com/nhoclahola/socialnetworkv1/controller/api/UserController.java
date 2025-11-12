package com.nhoclahola.socialnetworkv1.controller.api;

import com.nhoclahola.socialnetworkv1.dto.ApiResponse;
import com.nhoclahola.socialnetworkv1.dto.user.UserWithData;
import com.nhoclahola.socialnetworkv1.dto.user.request.UserUpdateRequest;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserWithDataResponse;
import com.nhoclahola.socialnetworkv1.service.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController
{
    private static final Logger logger = LogManager.getLogger(UserController.class);
    private final UserService userService;

    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> getAllUsers()
    {
        List<UserResponse> userResponses = userService.findAllUsers();
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResult(userResponses);
        return response;
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserWithDataResponse> getUserById(@PathVariable String id)
    {
        UserWithDataResponse user = userService.findUserDataByUserId(id);
        ApiResponse<UserWithDataResponse> response = new ApiResponse<>();
        response.setResult(user);
        return response;
    }

    @GetMapping("/users/me")
    public ApiResponse<UserWithDataResponse> getCurrentUser()
    {
        UserWithDataResponse user = userService.findCurrentUserData();
        ApiResponse<UserWithDataResponse> response = new ApiResponse<>();
        response.setResult(user);
        return response;
    }

    @PutMapping("/users/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable String id, @RequestBody @Valid UserUpdateRequest request)
    {
        UserResponse userResponse = userService.updateUser(id, request);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userResponse);
        return response;
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable String id)
    {
        return "Deleted user successfully";
    }

    @PostMapping("/users/follow/{userIdToFollow}")
    public ApiResponse<String> followUser(@PathVariable String userIdToFollow)
    {
        String result = userService.followUser(userIdToFollow);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }

    @GetMapping("/users/search")
    public ApiResponse<List<UserResponse>> searchUser(@RequestParam("query") String query, @RequestParam("index") int index, HttpServletRequest request)
    {
        // Log search queries for analytics and security monitoring
        String userAgent = request.getHeader("User-Agent");
        logger.info("User search query: '{}', User-Agent: {}", query, userAgent);
        
        List<UserResponse> userResponseList = userService.searchUser(query, index);
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResult(userResponseList);
        return response;
    }

    @GetMapping("/users/fromToken")
    public ApiResponse<UserResponse> getUserFromToken()
    {
        UserResponse userResponse = userService.findUserFromToken();
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userResponse);
        return response;
    }

    @PutMapping("/users/fromToken")
    public ApiResponse<UserResponse> updateUserFromToken(@RequestBody @Valid UserUpdateRequest request)
    {
        UserResponse userResponse = userService.updateUserFromToken(request);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userResponse);
        return response;
    }

    @GetMapping("/users/followings/{userId}")
    public ApiResponse<List<UserResponse>> getUsersFollowings(@PathVariable String userId, @RequestParam("index") int index)
    {

        List<UserResponse> userResponseList = userService.findUsersFollowing(userId, index);
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResult(userResponseList);
        return response;
    }

    @GetMapping("/users/followers/{userId}")
    public ApiResponse<List<UserResponse>> getUsersFollowers(@PathVariable String userId, @RequestParam("index") int index)
    {

        List<UserResponse> userResponseList = userService.findUsersFollower(userId, index);
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResult(userResponseList);
        return response;
    }

    @PostMapping("/users/avatar")
    public ApiResponse<UserResponse> uploadAvatar(@RequestPart MultipartFile image) throws IOException
    {
        UserResponse userResponse = userService.uploadAvatar(image);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userResponse);
        return response;
    }

    @PostMapping("/users/cover")
    public ApiResponse<UserResponse> uploadCover(@RequestPart MultipartFile image) throws IOException
    {
        UserResponse userResponse = userService.uploadCover(image);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userResponse);
        return response;
    }

    @GetMapping("/users/popular")
    public ApiResponse<List<UserResponse>> getSuggestedUsers()
    {
        List<UserResponse> userResponseList = userService.findSuggestedUsers();
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResult(userResponseList);
        return response;
    }

    @GetMapping("/users/latest-activity-followings")
    public ApiResponse<List<UserResponse>> getLatestActivityUsersFollowings()
    {
        List<UserResponse> userResponseList = userService.findLatestActivityUsersFollowings();
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResult(userResponseList);
        return response;
    }
}
