package swd.billiardshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import swd.billiardshop.dto.request.UserRegisterRequest;
import swd.billiardshop.dto.request.UserLoginRequest;
import swd.billiardshop.dto.request.UserUpdateRequest;
import swd.billiardshop.dto.request.AdminBanRequest;
import swd.billiardshop.dto.request.AdminChangeRoleRequest;
import swd.billiardshop.dto.response.UserResponse;
import swd.billiardshop.dto.response.ApiResponse;
import swd.billiardshop.service.UserService;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // Auth endpoints
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody UserRegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Register success", user));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Login success", token));
    }

    // User endpoints
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<UserResponse> users = userService. getAllUsers(page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", users));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Integer id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", user));
    }

    // Current authenticated user
    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Unauthenticated");
        return auth.getName();
    }

    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse user = userService.getCurrentUser(currentUsername());
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", user));
    }

    @PutMapping("/user/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(@RequestBody UserUpdateRequest request) {
        UserResponse user = userService.updateCurrentUser(currentUsername(), request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Update success", user));
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Integer id, @RequestBody UserUpdateRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Update success", user));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Delete success", null));
    }

    // --- Admin user management endpoints ---
    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<?>> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) swd.billiardshop.enums.Role role,
            @RequestParam(required = false) swd.billiardshop.enums.Status status,
            @RequestParam(required = false) java.time.LocalDateTime createdFrom,
            @RequestParam(required = false) java.time.LocalDateTime createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = userService.listUsersWithFilters(q, role, status, createdFrom, createdTo, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", result));
    }

    @PostMapping("/admin/users/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable Integer id, @RequestBody AdminBanRequest req) {
        userService.banUser(id, req.getReason());
        return ResponseEntity.ok(new ApiResponse<>(200, "User banned", null));
    }

    @PostMapping("/admin/users/{id}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable Integer id, @RequestBody AdminBanRequest req) {
        userService.unbanUser(id, req.getReason());
        return ResponseEntity.ok(new ApiResponse<>(200, "User unbanned", null));
    }

    @PostMapping("/admin/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(@PathVariable Integer id, @RequestBody AdminChangeRoleRequest req) {
        UserResponse u = userService.changeUserRole(id, req.getRole());
        return ResponseEntity.ok(new ApiResponse<>(200, "Role changed", u));
    }

    @GetMapping("/admin/users/{id}/activity")
    public ResponseEntity<ApiResponse<?>> getUserActivity(@PathVariable Integer id) {
        var data = userService.getUserActivity(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", data));
    }


    @PostMapping(value = "/user/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatarForCurrent(@RequestPart(value = "avatar", required = true) MultipartFile avatar) {
        try {
            UserResponse user = userService.uploadAvatarForUsername(currentUsername(), avatar);
            return ResponseEntity.ok(new ApiResponse<>(200, "Avatar uploaded successfully", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "Internal server error", null));
        }
    }


    @DeleteMapping("/user/me/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> removeAvatarForCurrent() {
        try {
            UserResponse user = userService.removeAvatarForUsername(currentUsername());
            return ResponseEntity.ok(new ApiResponse<>(200, "Avatar removed successfully", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "Internal server error", null));
        }
    }
}
