package com.nhoclahola.socialnetworkv1.dto.comment.request;

import javax.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CommentCreateRequest
{
    @NotBlank(message = "Comment can not be blank")
    @Length(max = 2000, message = "Maximum length for comment is 2000 characters")
    private String content;
}
