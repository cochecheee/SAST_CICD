package com.nhoclahola.socialnetworkv1.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailVerificationController
{
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String email) {
        return "Xác nhận email thành công cho " + email;
    }

    @GetMapping("/verify-reset")
    public String verifyReset(@RequestParam String email) {
        return "Xác nhận reset password thành công cho " + email;
    }
}
