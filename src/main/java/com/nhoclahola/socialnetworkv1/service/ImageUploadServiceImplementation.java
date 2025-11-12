package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageUploadServiceImplementation implements FileUploadService
{
    private final Tika tika;

    @Override
    public String upload(String path, MultipartFile file) throws IOException
    {
        if (file.isEmpty())
            throw new AppException(ErrorCode.IMAGE_IS_EMPTY);
        String fileType = tika.detect(file.getInputStream());

        if (fileType == null || !fileType.startsWith("image"))
            throw new AppException(ErrorCode.IMAGE_NOT_SUPPORTED);

        return this.createFile(path, file);
    }
}
