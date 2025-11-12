package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.comment.CommentWithData;
import com.nhoclahola.socialnetworkv1.dto.comment.request.CommentCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.comment.response.CommentResponse;
import com.nhoclahola.socialnetworkv1.dto.comment.response.CommentWithDataResponse;
import com.nhoclahola.socialnetworkv1.entity.Comment;
import com.nhoclahola.socialnetworkv1.entity.NotificationType;
import com.nhoclahola.socialnetworkv1.entity.Post;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.mapper.CommentMapper;
import com.nhoclahola.socialnetworkv1.repository.CommentRepository;
import com.nhoclahola.socialnetworkv1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImplement implements CommentService
{
    private final PostService postService;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentResponse createComment(String postId, CommentCreateRequest commentCreateRequest)
    {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findUserByEmail(userEmail);
        Post post = postService.findPostById(postId);
        Comment comment = commentMapper.commentCreateRequestToComment(commentCreateRequest);
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
        notificationService.createNotification(NotificationType.COMMENT, post.getUser(), user, post);
        return commentMapper.toCommentResponse(comment);
    }

    @Override
    public Comment findCommentById(String commentId)
    {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXIST));
    }

    @Override
    @Transactional
    public String likeComment(String commentId)
    {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findUserByEmail(userEmail);
        // To check valid of comment
        Comment comment = this.findCommentById(commentId);
        int isLiked = commentRepository.isLikedByUserId(commentId, user.getUserId());
        if (isLiked == 1)
        {
            commentRepository.removeLikeFromComment(commentId, user.getUserId());
            return "unliked";
        }
        else
        {
            commentRepository.addLikeToComment(commentId, user.getUserId());
            return "liked";
        }
    }

    @Override
    public CommentResponse findCommentByIdResponse(String commentId)
    {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXIST));
        return commentMapper.toCommentResponse(comment);
    }

    @Override
    public List<CommentWithDataResponse> findCommentsByPostId(String postId, int index)
    {
        String userRequestEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        // 1 page has 5 comments
        int pageNumber = index / 5;
        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by("createdAt").descending());
        List<CommentWithData> comments = commentRepository.findCommentsByPostId(userRequestEmail, postId, pageable);
        comments.sort(Comparator.comparing((comment) -> comment.getCreatedAt()));
        return commentMapper.toListCommentWithDataResponse(comments);
    }
}
