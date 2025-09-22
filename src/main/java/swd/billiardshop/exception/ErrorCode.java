package swd.billiardshop.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // ========== HTTP STATUS ERRORS (400-599) ==========
    UNAUTHORIZED(401, "Unauthorized access"),
    FORBIDDEN(403, "Access is forbidden"),
    NOT_FOUND(404, "Resource not found"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    SESSION_EXPIRED(503, "Session expired"),
    INVALID_DATE_RANGE(504, "Invalid date range"),
    // ========== GENERAL VALIDATION (9000-9999) ==========
    INVALID_REQUEST(9000, "Invalid request"),
    EMAIL_NOT_FOUND(9001, "Email does not exist"),
    TOKEN_INVALID(9002, "Invalid token"),
    TOKEN_MISMATCH(9003, "Token does not match email"),
    TOKEN_EXPIRED(9004, "Token has expired"),
    FILE_NULL_OR_EMPTY(9005, "File cannot be null or empty"),
    FILE_TOO_LARGE(9006, "File size too large. Maximum 10MB allowed"),
    FILE_INVALID_TYPE(9007, "Invalid file type. Only images are allowed"),
    EMAIL_SEND_FAILED(9008, "Failed to send email"),
    AVATAR_UPLOAD_FAILED(9009, "Failed to upload avatar"),
    HMAC_NULL(9010, "Key or data for HMAC cannot be null");

    private final int code;
    private final String message;

    public HttpStatus getHttpStatus() {
        return switch (code) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 500 -> HttpStatus.INTERNAL_SERVER_ERROR;
            case 503 -> HttpStatus.SERVICE_UNAVAILABLE;
            case 504 -> HttpStatus.GATEWAY_TIMEOUT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
