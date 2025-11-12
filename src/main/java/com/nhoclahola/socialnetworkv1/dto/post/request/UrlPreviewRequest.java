package com.nhoclahola.socialnetworkv1.dto.post.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlPreviewRequest {
    @NotBlank(message = "URL cannot be blank")
    private String url;
}

