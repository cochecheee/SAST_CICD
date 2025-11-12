package com.nhoclahola.socialnetworkv1.configuration;

import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer
{
    // For local storage
    private final ServerProperties serverProperties;
    private static String address;
    private static int port;
    public static String serverAddress;

    // For cloud storage
    private final ApplicationContext applicationContext;
    @Value("${cloud.aws.buket.name}")
    private String bucketNameInstance;

    private static S3Client s3Client;
    private static String bucketNameStatic;

    @PostConstruct
    private void init() throws UnknownHostException
    {
        address = serverProperties.getAddress() != null && !"0.0.0.0".equals(serverProperties.getAddress().getHostAddress())
                ? serverProperties.getAddress().getHostAddress()
                : InetAddress.getLocalHost().getHostAddress();
        port = serverProperties.getPort() != null ? serverProperties.getPort() : 8080;
        serverAddress = "http://" + address + ":" + port;
        s3Client = applicationContext.getBean(S3Client.class);
        bucketNameStatic = bucketNameInstance;
    }

    public static String getUrl(String objectKey)
    {
        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucketNameStatic)
                .key(objectKey).build();
        return s3Client.utilities().getUrl(getUrlRequest).toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }
}
