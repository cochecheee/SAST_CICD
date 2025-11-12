package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.auth.request.UserCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.request.UserResetPasswordRequest;
import com.nhoclahola.socialnetworkv1.dto.auth.response.AuthResponse;
import com.nhoclahola.socialnetworkv1.dto.auth.request.UserLoginRequest;
import javax.transaction.Transactional;

public interface AuthService
{
    public abstract AuthResponse authenticate(UserLoginRequest request);

    public abstract AuthResponse register(UserCreateRequest request);

    @Transactional
    AuthResponse resetPassword(UserResetPasswordRequest request);
}
