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
public class CloudStorageConfig {

    @Value("${aws.access.key.id:}")
    private String awsAccessKeyId;

    @Value("${aws.secret.access.key:}")
    private String awsSecretAccessKey;

    @Value("${aws.region:ap-southeast-1}")
    private String awsRegion;

    @Bean
    public S3Client generateS3Client() {
        if (awsAccessKeyId == null || awsAccessKeyId.isBlank() ||
                awsSecretAccessKey == null || awsSecretAccessKey.isBlank()) {
            throw new IllegalStateException("AWS credentials are required when aws.s3.enabled=true");
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                awsAccessKeyId,
                awsSecretAccessKey
        );

        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
