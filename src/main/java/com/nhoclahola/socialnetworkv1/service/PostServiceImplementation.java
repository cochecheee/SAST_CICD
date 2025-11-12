package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.post.PostWithData;
import com.nhoclahola.socialnetworkv1.dto.post.response.PostResponse;
import com.nhoclahola.socialnetworkv1.dto.post.response.PostWithDataResponse;
import com.nhoclahola.socialnetworkv1.entity.NotificationType;
import com.nhoclahola.socialnetworkv1.entity.Post;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.mapper.PostMapper;
import com.nhoclahola.socialnetworkv1.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostServiceImplementation implements PostService
{
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    // Relative path in project
    private static final String POST_DIR = "/posts/";
    private final VideoAwsS3UploadServiceImplementation videoAwsS3UploadServiceImplementation;
    private final ImageAwsS3UploadServiceImplementation imageAwsS3UploadServiceImplementation;

    @Override
    @Transactional
    public PostResponse createNewPost(String caption, MultipartFile file) throws IOException
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        String uploadPostDir;
        String imageUrl = null;
        String videoUrl = null;
        // Quick check, it will be check binary type in Upload Service
        if (file.getContentType() == null)
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        if (file.getContentType().startsWith("image"))
        {
            uploadPostDir = POST_DIR + currentUser.getUserId() + "/images/";
            imageUrl = imageAwsS3UploadServiceImplementation.upload(uploadPostDir, file);
        }
        else if (file.getContentType().startsWith("video"))
        {
            uploadPostDir = POST_DIR + currentUser.getUserId() + "/videos/";
            videoUrl = videoAwsS3UploadServiceImplementation.upload(uploadPostDir, file);
        }
        Post newPost = Post.builder()
                .caption(caption)
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .build();
        postRepository.save(newPost);
        return postMapper.toPostResponse(newPost);
    }

    @Override
    @Transactional
    public String deletePost(String postId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_EXIST));
        if (post.getUser() != currentUser)
            throw new RuntimeException("You can't delete this post");
        postRepository.delete(post);
        return "Post have been deleted successful";
    }

    @Override
    public List<PostWithDataResponse> findPostsByUserId(String userId, int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        // page = index / size
        // By default, get 10 posts from the user
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        List<PostWithData> posts = postRepository.findPostByUserId(currentUserEmail, userId, pageable);
        return postMapper.toListPostWithDataResponse(posts);
    }

    @Override
    public Post findPostById(String postId)
    {
        return postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_EXIST));
    }

    @Override
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<PostResponse> findAllPosts()
    {
        List<Post> posts = postRepository.findAll();
        return postMapper.toListPostResponse(posts);
    }

    @Override
    @Transactional
    public String likePost(String postId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        // To check valid of post
        Post post = this.findPostById(postId);
        int isLiked = postRepository.isLikedByUserId(postId, currentUser.getUserId());
        if (isLiked == 0)
        {
            postRepository.addLikeToPost(postId, currentUser.getUserId());
            // Intend to be Asynchronous
            notificationService.createNotification(NotificationType.LIKE, post.getUser(), currentUser, post);
            return "liked";
        }
        else
        {
            postRepository.removeLikeFromPost(postId, currentUser.getUserId());
            return "unliked";
        }
    }

    @Override
    @Transactional
    public String savePost(String postId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        // To check valid of post
        Post post = this.findPostById(postId);
        int isSaved = postRepository.isSavedByUserId(postId, currentUser.getUserId());
        if (isSaved == 0)
        {
            postRepository.savePost(postId, currentUser.getUserId());
            return "saved";
        }
        else
        {
            postRepository.unsavePost(postId, currentUser.getUserId());
            return "unsaved";
        }
    }

    @Override
    public PostResponse findPostByIdResponse(String postId)
    {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_EXIST));
        return postMapper.toPostResponse(post);
    }

    @Override
    public List<PostWithDataResponse> getHomeFeeds(int index) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        // page = index / size
        // By default, get 10 posts
        int followedPageNumber = index / 10;
        Pageable followedPageable = PageRequest.of(followedPageNumber, 10, Sort.by("createdAt").descending());
        List<PostWithData> followedPosts = postRepository.findPostsFromFollowedUsers(currentUserEmail, followedPageable);
        Set<PostWithData> homeFeeds = new HashSet<>(followedPosts);
        return postMapper.toListPostWithDataResponse(homeFeeds);
    }

    @Override
    public List<PostWithDataResponse> getCommunitiesFeeds(int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        // page = index / size
        // By default, get 10 posts
        int randomPostPageNumber = index / 10;
        Pageable randomPageable = PageRequest.of(randomPostPageNumber, 10);
        List<PostWithData> randomPosts = postRepository.findRandomPostsFromOtherUsers(currentUserEmail, randomPageable);
        Set<PostWithData> communitiesFeeds = new HashSet<>(randomPosts);
        return postMapper.toListPostWithDataResponse(communitiesFeeds);
    }

    @Override
    public List<PostWithDataResponse> findPopularVideoPosts(int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
//        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(30);
        List<PostWithData> popularVideoPosts = postRepository.findPopularVideoPosts(currentUserEmail, pageable);
        return postMapper.toListPostWithDataResponse(popularVideoPosts);
    }

    @Override
    public List<PostWithDataResponse> searchPost(String query, int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<PostWithData> posts = postRepository.searchPost(currentUserEmail, query, pageable);
        return postMapper.toListPostWithDataResponse(posts);
    }

    @Override
    public List<PostWithDataResponse> findUsersVideoPosts(String userId, int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<PostWithData> posts = postRepository.findVideoPostsByUserId(userId, currentUserEmail, pageable);
        return postMapper.toListPostWithDataResponse(posts);
    }

    @Override
    public List<PostWithDataResponse> findUsersSavedPosts(String userId, int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<PostWithData> posts = postRepository.findSavedPostsByUserId(userId, currentUserEmail, pageable);
        return postMapper.toListPostWithDataResponse(posts);
    }

    @Override
    public PostWithDataResponse findPostWithDataByPostId(String postId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        PostWithData post = postRepository.findPostWithDataById(currentUserEmail, postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXIST));
        return postMapper.toPostWithDataResponse(post);
    }

    @Override
    public List<PostWithDataResponse> findCurrentUserPosts(int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        // page = index / size
        // By default, get 10 posts from the user
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
        List<PostWithData> posts = postRepository.findPostByEmail(currentUserEmail, pageable);
        return postMapper.toListPostWithDataResponse(posts);
    }

    @Override
    public List<PostWithDataResponse> findCurrentUsersVideoPosts(int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<PostWithData> posts = postRepository.findVideoPostsByEmail(currentUserEmail, pageable);
        return postMapper.toListPostWithDataResponse(posts);
    }

    @Override
    public List<PostWithDataResponse> findCurrentUsersSavedPosts(int index)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        int pageNumber = index / 10;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        List<PostWithData> posts = postRepository.findSavedPostsByEmail(currentUserEmail, pageable);
        return postMapper.toListPostWithDataResponse(posts);
    }
}
