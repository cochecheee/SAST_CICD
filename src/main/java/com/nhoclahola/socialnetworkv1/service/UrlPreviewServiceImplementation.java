package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.post.response.UrlPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlPreviewServiceImplementation implements UrlPreviewService {
    private static final Logger logger = LogManager.getLogger(UrlPreviewServiceImplementation.class);

    @Override
    public UrlPreviewResponse fetchUrlPreview(String urlString) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // VULNERABILITY: No URL validation - allows SSRF attacks
        // This intentionally allows:
        // - Internal IPs (127.0.0.1, 192.168.x.x, 10.x.x.x, 169.254.x.x)
        // - localhost
        // - file:// protocol
        // - Internal services
        // - AWS/GCP/Azure metadata endpoints

        logger.info("URL Preview requested by user: {} - Target URL: {}", userEmail, urlString);

        try {
            // SSRF VULNERABILITY: Direct connection without validation
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            // Allow redirects - can be exploited for DNS rebinding
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Social Network Bot)");

            int statusCode = connection.getResponseCode();
            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();

            logger.info("URL fetched successfully - User: {} - URL: {} - Status: {} - ContentType: {}",
                       userEmail, urlString, statusCode, contentType);

            String title = "";
            String description = "";
            String imageUrl = null;

            // Check if response is HTML
            if (contentType != null && contentType.toLowerCase().contains("text/html")) {
                // Parse HTML to extract metadata
                Document doc = Jsoup.connect(urlString)
                        .userAgent("Mozilla/5.0 (Social Network Bot)")
                        .timeout(5000)
                        .followRedirects(true)
                        .get();

                title = doc.title();
                description = getMetaTag(doc, "description");
                imageUrl = getMetaTag(doc, "image");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = doc.select("img").first() != null ?
                               doc.select("img").first().absUrl("src") : null;
                }
            } else {
                // For non-HTML content (text/plain, application/json, etc.)
                // Read raw content - THIS EXPOSES SSRF DATA
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
                );
                String rawContent = reader.lines()
                    .limit(100) // Limit to first 100 lines to avoid huge responses
                    .collect(Collectors.joining("\n"));
                reader.close();

                // Set title based on content type
                if (contentType != null) {
                    if (contentType.contains("text/plain")) {
                        title = "Plain Text Response";
                    } else if (contentType.contains("application/json")) {
                        title = "JSON Response";
                    } else if (contentType.contains("application/xml")) {
                        title = "XML Response";
                    } else {
                        title = contentType;
                    }
                }

                // Put raw content in description - EXPOSES INTERNAL DATA
                description = rawContent;

                logger.warn("Non-HTML content fetched - User: {} - URL: {} - ContentType: {} - Preview: {}",
                           userEmail, urlString, contentType,
                           rawContent.length() > 100 ? rawContent.substring(0, 100) + "..." : rawContent);
            }

            logger.info("URL metadata extracted - User: {} - URL: {} - Title: {}",
                       userEmail, urlString, title);

            return UrlPreviewResponse.builder()
                    .url(urlString)
                    .title(title)
                    .description(description)
                    .imageUrl(imageUrl)
                    .statusCode(statusCode)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

        } catch (IOException e) {
            logger.error("Failed to fetch URL - User: {} - URL: {} - Error: {}",
                        userEmail, urlString, e.getMessage());

            // Return error info which can leak internal network information
            return UrlPreviewResponse.builder()
                    .url(urlString)
                    .title("Failed to fetch URL")
                    .description("Error: " + e.getMessage())
                    .statusCode(-1)
                    .build();
        }
    }

    private String getMetaTag(Document doc, String attr) {
        String content = doc.select("meta[name=" + attr + "]").attr("content");
        if (content == null || content.isEmpty()) {
            content = doc.select("meta[property=og:" + attr + "]").attr("content");
        }
        if (content == null || content.isEmpty()) {
            content = doc.select("meta[property=twitter:" + attr + "]").attr("content");
        }
        return content;
    }
}
