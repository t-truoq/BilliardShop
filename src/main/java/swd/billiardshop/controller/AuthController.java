package swd.billiardshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd.billiardshop.dto.response.ApiResponse;
import swd.billiardshop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import swd.billiardshop.dto.request.PasswordResetRequest;
import swd.billiardshop.dto.request.PasswordResetSubmitRequest;
import swd.billiardshop.dto.request.EmailVerificationRequest;
import swd.billiardshop.dto.request.EmailVerifySubmitRequest;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

        private static final Logger log = LoggerFactory.getLogger(AuthController.class);


                        @Operation(summary = "Yêu cầu đặt lại mật khẩu", description = "Gửi OTP về email người dùng.",
                                requestBody = @RequestBody(content = @Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PasswordResetRequest.class))),
                                responses = {
                                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP đã được gửi")
                                }
                        )
                        @PostMapping(value = "/request-password-reset", consumes = MediaType.APPLICATION_JSON_VALUE)
                        public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@org.springframework.web.bind.annotation.RequestBody PasswordResetRequest req) {
                                log.info("[request-password-reset] email param received: {}", req.getEmail());
                                userService.requestPasswordReset(req.getEmail());
                                return ResponseEntity.ok(new ApiResponse<>(200, "OTP đã được gửi", null));
                        }


                        @Operation(summary = "Đặt lại mật khẩu", description = "Xác thực OTP và đổi mật khẩu mới.",
                                requestBody = @RequestBody(content = @Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PasswordResetSubmitRequest.class))),
                                responses = {
                                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mật khẩu đã được cập nhật")
                                }
                        )
                        @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
                        public ResponseEntity<ApiResponse<Void>> resetPassword(@org.springframework.web.bind.annotation.RequestBody PasswordResetSubmitRequest req) {
                                log.info("[reset-password] params received: email={}, token={}, newPassword(length)={}", req.getEmail(), req.getToken(), req.getNewPassword() != null ? req.getNewPassword().length() : null);
                                userService.resetPassword(req.getEmail(), req.getToken(), req.getNewPassword());
                                return ResponseEntity.ok(new ApiResponse<>(200, "Mật khẩu đã được cập nhật", null));
                        }


                        @Operation(summary = "Yêu cầu xác thực email", description = "Gửi mã xác thực về email người dùng.",
                                requestBody = @RequestBody(content = @Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = EmailVerificationRequest.class))),
                                responses = {
                                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mã xác thực đã được gửi")
                                }
                        )
                        @PostMapping(value = "/request-email-verification", consumes = MediaType.APPLICATION_JSON_VALUE)
                        public ResponseEntity<ApiResponse<Void>> requestEmailVerification(@org.springframework.web.bind.annotation.RequestBody EmailVerificationRequest req) {
                                log.info("[request-email-verification] email param received: {}", req.getEmail());
                                userService.requestEmailVerification(req.getEmail());
                                return ResponseEntity.ok(new ApiResponse<>(200, "Mã xác thực đã được gửi", null));
                        }


                        @Operation(summary = "Xác thực email", description = "Xác thực email bằng mã OTP.",
                                requestBody = @RequestBody(content = @Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = EmailVerifySubmitRequest.class))),
                                responses = {
                                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email đã được xác thực")
                                }
                        )
                        @PostMapping(value = "/verify-email", consumes = MediaType.APPLICATION_JSON_VALUE)
                        public ResponseEntity<ApiResponse<Void>> verifyEmail(@org.springframework.web.bind.annotation.RequestBody EmailVerifySubmitRequest req) {
                                log.info("[verify-email] params received: email={}, token={}", req.getEmail(), req.getToken());
                                userService.verifyEmail(req.getEmail(), req.getToken());
                                return ResponseEntity.ok(new ApiResponse<>(200, "Email đã được xác thực", null));
                        }
}