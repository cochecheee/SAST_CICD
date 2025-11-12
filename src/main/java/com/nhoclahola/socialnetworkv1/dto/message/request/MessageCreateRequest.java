package com.nhoclahola.socialnetworkv1.dto.message.request;

import javax.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest
{
    @NotBlank(message = "Message can not be blank")
    @Length(max = 3000, message = "Maximum length for message is 3000 characters")
    private String content;
    private String imageUrl;
}
