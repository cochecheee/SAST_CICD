package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoAwsS3UploadServiceImplementation implements FileUploadService
{
    private final Tika tika;
    private final S3Client s3Client;

    @Value("${cloud.aws.buket.name}")
    private String bucketName;

    @Override
    public String upload(String path, MultipartFile file) throws IOException
    {
        String s3Path = UPLOAD_DIR + path;
        if (file.isEmpty())
            throw new AppException(ErrorCode.VIDEO_IS_EMPTY);
        String fileType = tika.detect(file.getInputStream());
        if (fileType == null || !fileType.startsWith("video"))
            throw new AppException(ErrorCode.VIDEO_NOT_SUPPORTED);
        return this.createFileOnS3(s3Path, fileType, file);
    }

    private String createFileOnS3(String path, String fileType, MultipartFile file) throws IOException
    {
        String fileHexName = UUID.randomUUID().toString().replace("-", "");
        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        String filePath = (path + fileHexName + extension).replaceAll("/+", "/");
        System.out.println(filePath);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .contentType(fileType)
                .bucket(bucketName)
                .key(filePath)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        return filePath;
    }
}
