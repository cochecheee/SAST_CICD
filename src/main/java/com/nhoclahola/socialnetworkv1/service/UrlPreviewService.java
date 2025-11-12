package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.post.response.UrlPreviewResponse;

public interface UrlPreviewService {
    UrlPreviewResponse fetchUrlPreview(String url);
}

