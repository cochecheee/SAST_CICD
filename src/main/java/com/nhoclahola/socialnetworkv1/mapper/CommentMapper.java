package com.nhoclahola.socialnetworkv1.mapper;

import com.nhoclahola.socialnetworkv1.dto.comment.CommentWithData;
import com.nhoclahola.socialnetworkv1.dto.comment.request.CommentCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.comment.response.CommentResponse;
import com.nhoclahola.socialnetworkv1.dto.comment.response.CommentWithDataResponse;
import com.nhoclahola.socialnetworkv1.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CommentMapper
{
    public abstract Comment commentCreateRequestToComment(CommentCreateRequest request);

    public abstract CommentResponse toCommentResponse(Comment comment);

    public abstract List<CommentResponse> toListCommentResponse(List<Comment> comments);

    @Mapping(source = "liked", target = "isLiked")
    public abstract CommentWithDataResponse toCommentWithDataResponse(CommentWithData comment);

    public abstract List<CommentWithDataResponse> toListCommentWithDataResponse(List<CommentWithData> comments);
}
