package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.auth.request.UserCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.request.UserResetPasswordRequest;
import com.nhoclahola.socialnetworkv1.dto.user.UserWithData;
import com.nhoclahola.socialnetworkv1.dto.user.request.UserUpdateRequest;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserWithDataResponse;
import com.nhoclahola.socialnetworkv1.entity.Role;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.mapper.UserMapper;
import com.nhoclahola.socialnetworkv1.repository.UserRepository;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final ImageAwsS3UploadServiceImplementation imageAwsS3UploadServiceImplementation;

    private final String AVATAR_DIR = "/avatars/";
    private final String COVER_DIR = "/covers/";
    @Override
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserResponse> findAllUsers()
    {
        List<User> users = userRepository.findAll();
        return userMapper.toListUserResponse(users);
    }

    @Override
    @Transactional
    public User createUser(UserCreateRequest request)
    {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXIST_REGISTER);
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USERNAME_EXIST_REGISTER);
        emailService.sendConfirmationEmail(request.getEmail());
        User user = userMapper.userLoginRequestToUser(request);
        // userId is already null after mapping,
        // Just don't set it, Hibernate won't check it is exist or not again
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    @Override
    public User resetPassword(UserResetPasswordRequest request)
    {
        User user = this.findUserByEmail(request.getEmail());
        emailService.sendResetPasswordCofrim(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return userRepository.save(user);
    }

    @Override
    public User findUserById(String userId)
    {
        return userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));
    }

    @Override
    public User findUserByEmail(String email)
    {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));
    }

    @Override
    public UserResponse findUserByIdResponse(String userId)
    {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserWithDataResponse findUserDataByUserId(String userId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserWithData user = userRepository.findUserWithDataByUserId(currentUserEmail, userId).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));
        return userMapper.toUserWithDataResponse(user);
    }

    @Override
    public UserWithDataResponse findCurrentUserData()
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserWithData user = userRepository.findUserWithDataByEmail(currentUserEmail).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));
        return userMapper.toUserWithDataResponse(user);
    }

    @Override
    @Transactional
    public String followUser(String userIdToFollow)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = this.findUserByEmail(currentUserEmail);
        if (currentUser.getUserId().equals(userIdToFollow))
            throw new AppException(ErrorCode.FOLLOW_YOURSELF);
        User userToFollow = userRepository.findById(userIdToFollow).orElseThrow(() ->
                new RuntimeException("User " + userIdToFollow + " is not exist"));
        if (!userRepository.isFollow(currentUser.getUserId(), userToFollow.getUserId()))
        {
            userRepository.follow(currentUser.getUserId(), userToFollow.getUserId());
            return "followed";
        }
        else
        {
            userRepository.unfollow(currentUser.getUserId(), userToFollow.getUserId());
            return "unfollowed";
        }
    }

    @Override
    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request)
    {
        User oldUser = userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));
        userMapper.updateUser(oldUser, request);
        userRepository.save(oldUser);
        return userMapper.toUserResponse(oldUser);
    }

    @Override
    public List<UserResponse> searchUser(String query, int index)
    {
        int pageNumber = index/ 5;
        Pageable pageable = PageRequest.of(pageNumber, 5);
        List<User> users = userRepository.searchUser(query, pageable);
        return userMapper.toListUserResponse(users);
    }

    @Override
    public UserResponse findUserFromToken()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = this.findUserByEmail(email);
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateUserFromToken(UserUpdateRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = this.findUserByEmail(email);
        userMapper.updateUser(user, request);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> findUsersFollowing(String userId, int index)
    {
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<User> usersFollowing = userRepository.findUsersFollowings(userId, pageable);
        return userMapper.toListUserResponse(usersFollowing);
    }

    @Override
    public List<UserResponse> findUsersFollower(String userId, int index)
    {
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<User> usersFollowers = userRepository.findUsersFollowers(userId, pageable);
        return userMapper.toListUserResponse(usersFollowers);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(MultipartFile image) throws IOException
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = this.findUserByEmail(currentUserEmail);
        String uploadAvatarDir = AVATAR_DIR + currentUser.getUserId() + "/";
        imageAwsS3UploadServiceImplementation.upload(uploadAvatarDir, image);
        String avatarUrl = imageAwsS3UploadServiceImplementation.upload(uploadAvatarDir, image);
        currentUser.setAvatarUrl(avatarUrl);
        userRepository.save(currentUser);
        return userMapper.toUserResponse(currentUser);
    }

    @Override
    @Transactional
    public UserResponse uploadCover(MultipartFile image) throws IOException
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = this.findUserByEmail(currentUserEmail);
        String uploadAvatarDir = COVER_DIR + currentUser.getUserId() + "/";
        String coverUrl = imageAwsS3UploadServiceImplementation.upload(uploadAvatarDir, image);
        currentUser.setCoverPhotoUrl(coverUrl);
        userRepository.save(currentUser);
        return userMapper.toUserResponse(currentUser);
    }

    @Override
    public List<UserResponse> findSuggestedUsers()
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(0, 5);
        List<User> userList = userRepository.findSuggestedUsers(currentUserEmail, pageable);
        return userMapper.toListUserResponse(userList);
    }

    @Override
    public List<UserResponse> findLatestActivityUsersFollowings()
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(0, 5);
        List<User> userList = userRepository.findLatestActivityUsersFollowings(currentUserEmail, pageable);
        return userMapper.toListUserResponse(userList);
    }

    @Override
    public List<UserResponse> findUsersLikedPostByPostId(String postId, int index)
    {
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<User> users = userRepository.findUsersLikedPost(postId, pageable);
        return userMapper.toListUserResponse(users);
    }

    @Override
    public List<UserResponse> findUsersLikedCommentByCommentId(String commentId, int index)
    {
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<User> users = userRepository.findUsersLikedComment(commentId, pageable);
        return userMapper.toListUserResponse(users);
    }

}
