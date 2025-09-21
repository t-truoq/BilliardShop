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
    INVALID_REQUEST(9000, "Invalid request");

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
