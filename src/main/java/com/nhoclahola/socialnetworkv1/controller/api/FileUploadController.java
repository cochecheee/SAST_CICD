package com.nhoclahola.socialnetworkv1.controller.api;

import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.repository.UserRepository;
import com.nhoclahola.socialnetworkv1.service.VideoUploadServiceImplementation;
import com.nhoclahola.socialnetworkv1.service.ImageUploadServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


// This is just for test
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FileUploadController
{
    // Absolute path
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    private static final String UPLOAD_POST_DIR = UPLOAD_DIR + "/posts/";

    private final UserRepository userRepository;
    private final VideoUploadServiceImplementation videoUploadServiceImplementation;
    private final ImageUploadServiceImplementation imageUploadServiceImplementation;
//    @PostMapping("/upload")
//    public String uploadFile(@RequestParam("image") MultipartFile image) throws IOException
//    {
//        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        System.out.println(userEmail);
//        User user = userRepository.findByEmail(userEmail).get();
//        return imageUploadServiceImplementation.upload(user.getUserId(), UPLOAD_POST_DIR, image);
//    }
//
//    @PostMapping("/upload/avatar")
//    public String uploadAvatar(@RequestParam("avatar") MultipartFile avatar) throws IOException
//    {
//        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        System.out.println(userEmail);
//        User user = userRepository.findByEmail(userEmail).get();
//        return imageUploadServiceImplementation.uploadAvatar(user.getUserId(), UPLOAD_DIR, avatar);
//    }

}
