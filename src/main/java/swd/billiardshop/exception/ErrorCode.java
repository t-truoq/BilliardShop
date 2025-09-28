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
    HMAC_NULL(9010, "Key or data for HMAC cannot be null"),
    SYSTEM_ERROR(1000, "Lỗi hệ thống"),

    VALIDATION_FAILED(1005, "Dữ liệu không hợp lệ"),

    // User Errors (2000-2999)
    USER_NOT_FOUND(2000, "Không tìm thấy người dùng"),
    USER_ALREADY_EXISTS(2001, "Người dùng đã tồn tại"),
    INVALID_CREDENTIALS(2002, "Thông tin đăng nhập không đúng"),
    USER_INACTIVE(2003, "Tài khoản chưa kích hoạt"),
    USER_BANNED(2004, "Tài khoản bị khóa"),

    // Product Errors (3000-3999)
    PRODUCT_NOT_FOUND(3000, "Không tìm thấy sản phẩm"),
    PRODUCT_OUT_OF_STOCK(3001, "Sản phẩm hết hàng"),
    PRODUCT_INACTIVE(3002, "Sản phẩm không còn bán"),
    INSUFFICIENT_STOCK(3003, "Không đủ hàng trong kho"),

    // Cart Errors (4000-4999)
    CART_NOT_FOUND(4000, "Không tìm thấy giỏ hàng"),
    CART_ITEM_NOT_FOUND(4001, "Không tìm thấy sản phẩm trong giỏ hàng"),
    CART_EMPTY(4002, "Giỏ hàng trống"),
    INVALID_QUANTITY(4003, "Số lượng không hợp lệ"),
    CART_ITEM_EXISTS(4004, "Sản phẩm đã có trong giỏ hàng"),

    // Order Errors (5000-5999)
    ORDER_NOT_FOUND(5000, "Không tìm thấy đơn hàng"),
    ORDER_CANNOT_CANCEL(5001, "Không thể hủy đơn hàng"),
    ORDER_ALREADY_CONFIRMED(5002, "Đơn hàng đã được xác nhận"),
    ORDER_ALREADY_CANCELLED(5003, "Đơn hàng đã bị hủy"),
    ORDER_PAYMENT_FAILED(5004, "Thanh toán thất bại"),
    INVALID_ORDER_STATUS(5005, "Trạng thái đơn hàng không hợp lệ"),

    // Payment Errors (6000-6999)
    PAYMENT_NOT_FOUND(6000, "Không tìm thấy thông tin thanh toán"),
    PAYMENT_FAILED(6001, "Thanh toán thất bại"),
    PAYMENT_EXPIRED(6002, "Thanh toán đã hết hạn"),
    PAYMENT_ALREADY_PROCESSED(6003, "Thanh toán đã được xử lý"),
    INVALID_PAYMENT_METHOD(6004, "Phương thức thanh toán không hợp lệ"),

    // Shipping Errors (7000-7999)
    SHIPPING_ADDRESS_INVALID(7000, "Địa chỉ giao hàng không hợp lệ"),
    SHIPPING_METHOD_NOT_AVAILABLE(7001, "Phương thức vận chuyển không khả dụng"),
    LOCATION_NOT_MAPPED(7001, "Địa chỉ chưa được ánh xạ tới mã khu vực GHN"),
    GHN_API_ERROR(7002, "Lỗi API giao hàng nhanh"),
    SHIPMENT_NOT_FOUND(7003, "Không tìm thấy thông tin vận chuyển"),
    CANNOT_CREATE_SHIPMENT(7004, "Không thể tạo đơn vận chuyển"),

    // Promotion Errors (8000-8999)
    PROMOTION_NOT_FOUND(8000, "Không tìm thấy mã giảm giá"),
    PROMOTION_EXPIRED(8001, "Mã giảm giá đã hết hạn"),
    PROMOTION_USED_UP(8002, "Mã giảm giá đã được sử dụng hết"),
    PROMOTION_NOT_APPLICABLE(8003, "Mã giảm giá không áp dụng được"),
    PROMOTION_MINIMUM_ORDER_NOT_MET(8004, "Chưa đạt giá trị đơn hàng tối thiểu");

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
