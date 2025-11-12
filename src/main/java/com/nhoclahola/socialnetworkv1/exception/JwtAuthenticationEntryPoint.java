package com.nhoclahola.socialnetworkv1.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhoclahola.socialnetworkv1.dto.ApiResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.io.PrintWriter;

// To catch Unauthenticated exception (When you do not have a token)
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint
{

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException
    {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setResponseCode(errorCode.getResponseCode());
        apiResponse.setMessage(errorCode.getMessage());
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        PrintWriter printWriter = response.getWriter();
        printWriter.write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
