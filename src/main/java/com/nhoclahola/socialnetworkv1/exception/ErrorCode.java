package com.nhoclahola.socialnetworkv1.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode
{
    // Auth and validation
    UNAUTHENTICATED(1100, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1101, "You do not have permission to do this", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1102, "Invalid Token", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1103, "Invalid request", HttpStatus.BAD_REQUEST),
    EMAIL_EXIST_REGISTER(1104, "This email has been used", HttpStatus.CONFLICT),
    USERNAME_EXIST_REGISTER(1105, "This username has been used", HttpStatus.CONFLICT),

    // User
    USER_NOT_EXIST(1200, "The user does not exist", HttpStatus.NOT_FOUND),
    FOLLOW_YOURSELF(1201, "You can't follow yourself", HttpStatus.BAD_REQUEST),

    // Post
    POST_NOT_EXIST(1300, "The post does not exist", HttpStatus.NOT_FOUND),

    // Comment
    COMMENT_NOT_EXIST(1400, "The comment does not exist", HttpStatus.BAD_REQUEST),

    // Video
    VIDEO_NOT_EXIST(1500, "The video does not exist", HttpStatus.BAD_REQUEST),

    // Chat
    CHAT_NOT_EXIST(1600, "The chat does not exist", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST_IN_CHAT(1601, "User does not exist in the chat", HttpStatus.BAD_REQUEST),

    // Message
    MESSAGE_NOT_EXIST(1700, "The message does not exist", HttpStatus.BAD_REQUEST),

    // File upload
    FILE_IS_EMPTY(1801, "The file you uploaded is empty", HttpStatus.BAD_REQUEST),
    IMAGE_IS_EMPTY(1801, "The image you uploaded is empty", HttpStatus.BAD_REQUEST),
    VIDEO_IS_EMPTY(1802, "The video you uploaded is empty", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_SUPPORTED(1803, "The image you uploaded is either not a valid image or is not supported", HttpStatus.BAD_REQUEST),
    VIDEO_NOT_SUPPORTED(1804, "The video you uploaded is either not a valid video or is not supported", HttpStatus.BAD_REQUEST),
    IO_ERROR(1805, "There is an error during the I/O process", HttpStatus.INTERNAL_SERVER_ERROR),

    // Notification
    NOTIFICATION_NOT_EXIST(1901, "The notification does not exist", HttpStatus.BAD_REQUEST),
    NOT_YOUR_NOTIFICATION(1902, "That not your notification", HttpStatus.BAD_REQUEST),

    ;
    private final int responseCode;
    private final String message;
    private final HttpStatus httpStatusCode;
}
