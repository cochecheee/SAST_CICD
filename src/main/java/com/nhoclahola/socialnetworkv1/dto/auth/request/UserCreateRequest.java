package com.nhoclahola.socialnetworkv1.dto.auth.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserCreateRequest
{
    @NotBlank(message = "You must enter the first name")
    @Length(max = 20, message = "Maximum characters for first name is 20")
    private String firstName;
    @NotBlank(message = "You must enter the last name")
    @Length(max = 40, message = "Maximum characters for last name is 40")
    private String lastName;
    @NotBlank(message = "You must enter the email")
    @Length(min = 6, max = 40, message = "Email's length must between 6 and 40")
    @Email(message = "This must be an email")
    private String email;
    @NotBlank(message = "You must enter the username")
    @Length(min = 6, max = 25, message = "Username's length must between 6 and 25")
    private String username;
    @NotBlank(message = "You must enter the password")
    @Length(min = 6, max = 40, message = "Password's length must between 6 and 40")
    private String password;
    private String gender;
}
