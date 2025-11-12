package com.nhoclahola.socialnetworkv1.dto.post.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlPreviewResponse {
    private String url;
    private String title;
    private String description;
    private String imageUrl;
    private Integer statusCode;
    private String contentType;
    private Long contentLength;
}

