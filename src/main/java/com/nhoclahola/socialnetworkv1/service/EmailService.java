package com.nhoclahola.socialnetworkv1.service;

import org.springframework.scheduling.annotation.Async;

public interface EmailService
{
    void sendConfirmationEmail(String toEmail);

    @Async
    void sendResetPasswordCofrim(String toEmail);
}
