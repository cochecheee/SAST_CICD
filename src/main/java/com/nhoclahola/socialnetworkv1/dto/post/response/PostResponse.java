package com.nhoclahola.socialnetworkv1.dto.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhoclahola.socialnetworkv1.dto.comment.response.CommentResponse;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PostResponse
{
    private String postId;
    private String caption;
    private String imageUrl;
    private String videoUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private UserResponse user;
}
