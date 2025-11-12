package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.admin.response.DashBoardInfo;
import com.nhoclahola.socialnetworkv1.dto.post.response.PostResponse;
import com.nhoclahola.socialnetworkv1.dto.user.request.AdminResetPassword;
import com.nhoclahola.socialnetworkv1.dto.user.request.AdminUpdateUser;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import com.nhoclahola.socialnetworkv1.entity.Post;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.mapper.PostMapper;
import com.nhoclahola.socialnetworkv1.mapper.UserMapper;
import com.nhoclahola.socialnetworkv1.repository.PostRepository;
import com.nhoclahola.socialnetworkv1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImplementation implements AdminService
{
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public DashBoardInfo getDashBoardInfo()
    {
        return userRepository.adminDashBoardInfo();
    }

    @Override
    public Page<UserResponse> getAllUsersAdmin(int page)
    {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<User> users = userRepository.findAllUsersAdmin(pageable);
        return userMapper.pageUserToPageUserResponse(users);
    }

    @Override
    public UserResponse adminUpdateUser(String userId, AdminUpdateUser request)
    {
        User user = userService.findUserById(userId);
        if (!user.getEmail().equals(request.getEmail() ))
        {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new AppException(ErrorCode.EMAIL_EXIST_REGISTER);
            else
                user.setEmail(request.getEmail());
        }
        if (!user.getUsername().equals(request.getUsername()))
        {
            if (userRepository.existsByUsername(request.getUsername()))
                throw new AppException(ErrorCode.USERNAME_EXIST_REGISTER);
            else
                user.setUsername(request.getUsername());
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDescription(request.getDescription());
        user.setGender(request.getGender());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse adminUpdatePassword(String userId, AdminResetPassword request)
    {
        User user = userService.findUserById(userId);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public Page<PostResponse> getAllPostsAdmin(int page)
    {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<Post> posts = postRepository.findAllPostsAdmin(pageable);
        return postMapper.pagePostToPagePostResponse(posts);
    }

    @Override
    @Transactional
    public String adminDeletePost(String postId)
    {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_EXIST));
        postRepository.delete(post);
        return "Post have been deleted successful";
    }

    @Override
    @Transactional
    public String adminDeleteUser(String userId)
    {
        User user = userService.findUserById(userId);
        userRepository.delete(user);
        return "User have been deleted successful";
    }
}
