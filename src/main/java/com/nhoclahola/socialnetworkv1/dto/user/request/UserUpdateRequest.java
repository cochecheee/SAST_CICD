package com.nhoclahola.socialnetworkv1.dto.user.request;

import javax.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserUpdateRequest
{
    @NotBlank(message = "You must enter the first name")
    @Length(max = 20, message = "Maximum characters for first name is 20")
    private String firstName;
    @NotBlank(message = "You must enter the last name")
    @Length(max = 40, message = "Maximum characters for last name is 40")
    private String lastName;
    @Length(min = 6, message = "Password must at least 6 characters")
    private String password;
    private Boolean gender;
}
