package com.nhoclahola.socialnetworkv1.dto.post.request;

import javax.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/***
 * Not used
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PostCreateRequest
{
    @NotBlank(message = "Caption can not be blank")
    @Length(max = 2000, message = "Maximum length for caption is 2000 characters")
    private String caption;
    private String imageUrl;
    private String videoUrl;
}
