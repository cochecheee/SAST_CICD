package com.nhoclahola.socialnetworkv1.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhoclahola.socialnetworkv1.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PostWithData
{
    private String postId;
    private String caption;
    private String imageUrl;
    private String videoUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private User user;
    private long likedCount;
    private long commentCount;
    private boolean isLiked;
    private boolean isSaved;
}
