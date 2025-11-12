package com.nhoclahola.socialnetworkv1.dto.admin.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DashBoardInfo
{
    private long userCount;
    private long postCount;
    private long chatCount;
    private long commentCount;
    private long postsHaveImage;
    private long postsHaveVideo;
}
