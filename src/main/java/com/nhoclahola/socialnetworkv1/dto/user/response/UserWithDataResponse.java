package com.nhoclahola.socialnetworkv1.dto.user.response;

import lombok.*;

// With full url

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserWithDataResponse
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
    private long posts;
    private long followers;
    private long followings;
    private boolean isFollow;
}
