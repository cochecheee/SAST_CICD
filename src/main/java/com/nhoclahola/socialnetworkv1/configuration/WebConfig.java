package com.nhoclahola.socialnetworkv1.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    // For local storage
    private final ServerProperties serverProperties;
    private static String address;
    private static int port;
    public static String serverAddress;

    // For cloud storage (OPTIONAL)
    private final ApplicationContext applicationContext;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${aws.bucket.name:}")
    private String bucketNameInstance;

    private static S3Client s3Client;
    private static String bucketNameStatic;
    private static boolean s3Available = false;

    @PostConstruct
    private void init() throws UnknownHostException {
        // Setup server address
        address = serverProperties.getAddress() != null && !"0.0.0.0".equals(serverProperties.getAddress().getHostAddress())
                ? serverProperties.getAddress().getHostAddress()
                : InetAddress.getLocalHost().getHostAddress();
        port = serverProperties.getPort() != null ? serverProperties.getPort() : 8080;
        serverAddress = "http://" + address + ":" + port;

        // Setup S3 if enabled
        if (s3Enabled) {
            try {
                s3Client = applicationContext.getBean(S3Client.class);
                bucketNameStatic = bucketNameInstance;
                s3Available = true;
                System.out.println("✅ S3Client initialized - Cloud storage enabled");
            } catch (Exception e) {
                System.out.println("⚠️ S3Client not available - Using local storage");
                s3Available = false;
            }
        } else {
            System.out.println("📁 Using local file storage");
            s3Available = false;
        }
    }

    public static String getUrl(String objectKey) {
        if (!s3Available || s3Client == null) {
            // Return local URL if S3 not available
            return serverAddress + "/" + objectKey;
        }

        try {
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucketNameStatic)
                    .key(objectKey)
                    .build();
            return s3Client.utilities().getUrl(getUrlRequest).toString();
        } catch (Exception e) {
            System.err.println("Error getting S3 URL for key: " + objectKey);
            // Fallback to local URL
            return serverAddress + "/" + objectKey;
        }
    }

    public static boolean isS3Available() {
        return s3Available;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from local directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }
}