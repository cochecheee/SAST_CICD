package com.nhoclahola.socialnetworkv1.dto.user.response;

import com.nhoclahola.socialnetworkv1.entity.Role;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserResponse
{
    private String userId;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String description;
    private Boolean gender;
    private String avatarUrl;
    private String coverPhotoUrl;
    private Role role;
}
