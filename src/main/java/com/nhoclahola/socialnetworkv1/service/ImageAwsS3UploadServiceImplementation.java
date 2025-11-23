package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class ImageAwsS3UploadServiceImplementation implements FileUploadService {

    private final Tika tika;
    private final S3Client s3Client;

    @Value("${aws.bucket.name:}")
    private String bucketName;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${file.storage.path:/app/uploads}")
    private String localStoragePath;

    // Constructor thủ công với @Autowired(required = false)
    @Autowired
    public ImageAwsS3UploadServiceImplementation(
            Tika tika,
            @Autowired(required = false) S3Client s3Client) {
        this.tika = tika;
        this.s3Client = s3Client;

        if (s3Enabled && s3Client == null) {
            System.out.println("⚠️ WARNING: S3 is enabled but S3Client is null. Falling back to local storage.");
        }

        System.out.println(s3Enabled ? "☁️ Using AWS S3 storage" : "📁 Using local file storage");
    }

    @Override
    public String upload(String path, MultipartFile file) throws IOException {
        String s3Path = UPLOAD_DIR + path;

        if (file.isEmpty()) {
            throw new AppException(ErrorCode.IMAGE_IS_EMPTY);
        }

        String fileType = tika.detect(file.getInputStream());
        if (fileType == null || !fileType.startsWith("image")) {
            throw new AppException(ErrorCode.IMAGE_NOT_SUPPORTED);
        }

        // Kiểm tra xem có dùng S3 không
        if (s3Enabled && s3Client != null) {
            return this.createFileOnS3(s3Path, fileType, file);
        } else {
            return this.createFileOnLocal(s3Path, fileType, file);
        }
    }

    private String createFileOnS3(String path, String fileType, MultipartFile file) throws IOException {
        String fileHexName = UUID.randomUUID().toString().replace("-", "");
        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        String filePath = (path + fileHexName + extension).replaceAll("/+", "/");

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .contentType(fileType)
                .bucket(bucketName)
                .key(filePath)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        System.out.println("✅ File uploaded to S3: " + filePath);
        return filePath;
    }

    private String createFileOnLocal(String path, String fileType, MultipartFile file) throws IOException {
        String fileHexName = UUID.randomUUID().toString().replace("-", "");
        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = fileHexName + extension;

        // Tạo đường dẫn đầy đủ
        Path uploadPath = Paths.get(localStoragePath, path);

        // Tạo thư mục nếu chưa tồn tại
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);

        // Lưu file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn tương đối
        String relativePath = (path + fileName).replaceAll("/+", "/");
        System.out.println("✅ File saved to local storage: " + filePath);

        return relativePath;
    }
}