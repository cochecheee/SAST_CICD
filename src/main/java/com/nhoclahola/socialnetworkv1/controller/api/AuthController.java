package com.nhoclahola.socialnetworkv1.controller.api;

import com.nhoclahola.socialnetworkv1.dto.auth.request.UserCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.request.UserLoginRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.request.UserResetPasswordRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.response.AuthResponse;
import com.nhoclahola.socialnetworkv1.service.AuthService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController
{
    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse createUser(@RequestBody @Valid UserCreateRequest request, HttpServletRequest httpRequest)
    {
        // Log user registration attempt with User-Agent for security monitoring
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = getClientIp(httpRequest);
        logger.info("New user registration attempt - Email: {}, IP: {}, User-Agent: {}", 
                    request.getEmail(), clientIp, userAgent);
        
        AuthResponse response = authService.register(request);
        logger.info("User registered successfully - Email: {}", request.getEmail());
        return response;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid UserLoginRequest request, HttpServletRequest httpRequest)
    {
        // Log login attempts for security auditing - User-Agent helps detect suspicious activity
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = getClientIp(httpRequest);
        logger.info("Login attempt - Email: {}, IP: {}, User-Agent: {}", 
                    request.getEmail(), clientIp, userAgent);
        
        AuthResponse response = authService.authenticate(request);
        logger.info("Login successful - Email: {}", request.getEmail());
        return response;
    }

    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@RequestBody @Valid UserResetPasswordRequest request, HttpServletRequest httpRequest)
    {
        // Log password reset for security tracking
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = getClientIp(httpRequest);
        logger.info("Password reset attempt - Email: {}, IP: {}, User-Agent: {}", 
                    request.getEmail(), clientIp, userAgent);
        
        AuthResponse response = authService.resetPassword(request);
        logger.info("Password reset successful - Email: {}", request.getEmail());
        return response;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
