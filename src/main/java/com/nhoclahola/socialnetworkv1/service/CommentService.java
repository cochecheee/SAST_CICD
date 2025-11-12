package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.comment.request.CommentCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.comment.response.CommentResponse;
import com.nhoclahola.socialnetworkv1.dto.comment.response.CommentWithDataResponse;
import com.nhoclahola.socialnetworkv1.entity.Comment;

import java.util.List;

public interface CommentService
{
    public CommentResponse createComment(String postId, CommentCreateRequest commentCreateRequest);

    public Comment findCommentById(String commentId);

    public String likeComment(String commentId);

    public CommentResponse findCommentByIdResponse(String commentId);

    public List<CommentWithDataResponse> findCommentsByPostId(String postId, int index);
}
