package com.nhoclahola.socialnetworkv1.dto.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhoclahola.socialnetworkv1.dto.user.response.UserResponse;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CommentResponse
{
    private String commentId;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private UserResponse user;
}
