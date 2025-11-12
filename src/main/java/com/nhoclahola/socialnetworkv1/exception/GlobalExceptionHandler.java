package com.nhoclahola.socialnetworkv1.exception;

import com.nhoclahola.socialnetworkv1.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception)
    {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setResponseCode(errorCode.getResponseCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }

    // Spring validation exception
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> MethodArgumentNotValidException(MethodArgumentNotValidException exception)
    {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String errors = exception.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError) error).getField() + " - " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setResponseCode(errorCode.getResponseCode());
        apiResponse.setMessage(errorCode.getMessage() + ": " + errors);

        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }

    // For wrong json request
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handlingAppException(HttpMessageNotReadableException exception)
    {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setResponseCode(errorCode.getResponseCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }

    // Multipart exception, such as required part key
    @ExceptionHandler(value = MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<?>> MissingServletRequestPartExceptionException(MissingServletRequestPartException exception)
    {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setResponseCode(errorCode.getResponseCode());
        apiResponse.setMessage(errorCode.getMessage() + ": " + exception.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }

    // Handling AccessDeniedException (not in java.nio.file) when you are not authorized to access an endpoint
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(AccessDeniedException exception)
    {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setResponseCode(errorCode.getResponseCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }

    // Handling IOException when uploading file
//    @ExceptionHandler(value = IOException.class)
//    public ResponseEntity<ApiResponse<?>> handlingIOException(IOException exception)
//    {
//        ErrorCode errorCode = ErrorCode.IO_ERROR;
//        ApiResponse<Object> apiResponse = new ApiResponse<>();
//        apiResponse.setResponseCode(errorCode.getResponseCode());
//        apiResponse.setMessage(errorCode.getMessage());
//        return ResponseEntity.status(errorCode.getHttpStatusCode())
//                .body(apiResponse);
//    }
}
