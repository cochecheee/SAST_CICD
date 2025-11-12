package com.nhoclahola.socialnetworkv1.dto.user.request;

import javax.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminResetPassword
{
    @NotBlank(message = "You must enter the password")
    @Length(min = 6, max = 40, message = "Password's length must between 6 and 40")
    private String password;
}
