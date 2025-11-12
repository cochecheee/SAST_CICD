package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.auth.request.UserCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.request.UserResetPasswordRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.response.AuthResponse;
import com.nhoclahola.socialnetworkv1.dto.auth.request.UserLoginRequest;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.security.JwtProvider;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService
{
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse authenticate(UserLoginRequest request)
    {
        User user = userService.findUserByEmail(request.getEmail());
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        String jwtToken = JwtProvider.generateJwtToken(user);
        return new AuthResponse(jwtToken, "Login successfully");
    }

    @Override
    @Transactional
    public AuthResponse register(UserCreateRequest request)
    {
        User user = userService.createUser(request);
        String jwtToken = JwtProvider.generateJwtToken(user);
        return new AuthResponse(jwtToken, "Register successfully");
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(UserResetPasswordRequest request)
    {
        User user = userService.resetPassword(request);
        String jwtToken = JwtProvider.generateJwtToken(user);
        return new AuthResponse(jwtToken, "Reset passoword successfully");
    }
}
