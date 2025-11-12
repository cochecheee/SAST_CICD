package com.nhoclahola.socialnetworkv1.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhoclahola.socialnetworkv1.dto.ApiResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

// Because filters execute before the Controller
// GlobalExceptionHandler can't catch exception thrown in filters
// Therefore, I need to create a filter to catch exceptions thrown by other filters
// (Catch invalid token)
public class FilterExceptionHandler extends OncePerRequestFilter
{

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        try
        {
            filterChain.doFilter(request, response);
        }
        catch (AppException exception)
        {
            ErrorCode errorCode = exception.getErrorCode();
            ApiResponse<?> apiResponse = new ApiResponse<>();
            apiResponse.setResponseCode(errorCode.getResponseCode());
            apiResponse.setMessage(errorCode.getMessage());
            response.setContentType("application/json");
            response.setStatus(errorCode.getHttpStatusCode().value());
            ObjectMapper objectMapper = new ObjectMapper();
            PrintWriter printWriter = response.getWriter();
            printWriter.write(objectMapper.writeValueAsString(apiResponse));
            response.flushBuffer();
        }
    }
}
