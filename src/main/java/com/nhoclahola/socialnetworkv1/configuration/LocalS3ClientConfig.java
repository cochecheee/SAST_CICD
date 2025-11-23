package com.nhoclahola.socialnetworkv1.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class LocalS3ClientConfig {

    @Bean
    @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "false", matchIfMissing = true)
    public S3Client localS3Client() {
        System.out.println("📦 Creating Local S3Client with fake credentials...");

        // Tạo S3Client với fake credentials (không connect thật)
        S3Client client = S3Client.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("fake-access-key", "fake-secret-key")
                ))
                .build();

        System.out.println("✅ Local S3Client created (will not connect to AWS)");
        return client;
    }
}