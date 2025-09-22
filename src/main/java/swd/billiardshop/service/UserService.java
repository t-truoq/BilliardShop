package swd.billiardshop.service;

import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;
import swd.billiardshop.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.billiardshop.dto.request.UserRegisterRequest;
import swd.billiardshop.dto.request.UserLoginRequest;
import swd.billiardshop.dto.request.UserUpdateRequest;
import swd.billiardshop.dto.response.UserResponse;
import swd.billiardshop.entity.User;
import swd.billiardshop.mapper.UserMapper;
import swd.billiardshop.repository.UserRepository;
import swd.billiardshop.repository.VerificationTokenRepository;
import swd.billiardshop.entity.VerificationToken;
import swd.billiardshop.dto.request.MailBody;

import java.io.IOException;
import java.time.LocalDateTime;
import swd.billiardshop.configuration.JwtUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import swd.billiardshop.enums.Status;
import org.springframework.data.jpa.domain.Specification;
import swd.billiardshop.repository.UserSpecifications;
import swd.billiardshop.enums.Role;
import swd.billiardshop.enums.Status;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;
   
    private final VerificationTokenRepository tokenRepository;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        // Kiểm tra username và email đã tồn tại
        if (userRepository.findByUsername(request.getUsername()).isPresent() ||
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Username or email already exists");
        }

        // Tạo user mới
        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(Status.ACTIVE); // Hoặc Status.INACTIVE nếu muốn user phải verify email mới active

        // Lưu user vào database
        userRepository.save(user);

        try {
            // Tự động gửi email verification sau khi đăng ký thành công
            requestEmailVerification(user.getEmail());
        } catch (Exception e) {
            // Log lỗi nhưng không throw exception để không ảnh hưởng đến quá trình đăng ký
            // Có thể sử dụng logger để ghi log
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return userMapper.toUserResponse(user);
    }

    public String login(UserLoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid username or password");
        }

        User user = userOpt.get();

        // Kiểm tra trạng thái banned
        if (user.getStatus() == Status.BANNED) {
            throw new AppException(ErrorCode.FORBIDDEN, "Account is banned");
        }

        // Kiểm tra email verification
        if (user.getEmailVerifiedAt() == null && user.getRole() != Role.ADMIN) {
            throw new AppException(ErrorCode.FORBIDDEN, "Email not verified. Please verify your email before logging in.");
        }

        // Cập nhật lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getUserId());
    }

    public List<UserResponse> getAllUsers(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Integer id) {
    User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        return userMapper.toUserResponse(user);
    }

    // Helper to return entity (used by controllers/services)
    public User getUserEntityById(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
    }

    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
    }

    // Operations for the authenticated user (by username)
    public UserResponse getCurrentUser(String username) {
        User u = getUserEntityByUsername(username);
        return userMapper.toUserResponse(u);
    }

    public UserResponse updateCurrentUser(String username, UserUpdateRequest request) {
        User u = getUserEntityByUsername(username);
        if (request.getEmail() != null) u.setEmail(request.getEmail());
        if (request.getPassword() != null) u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(u);
        return userMapper.toUserResponse(u);
    }

    public UserResponse uploadAvatarForUsername(String username, MultipartFile avatarFile) {
        User u = getUserEntityByUsername(username);
        try {
            String avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars", "avatar");
            u.setAvatarUrl(avatarUrl);
            u.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(u);
            return userMapper.toUserResponse(u);
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to upload avatar: " + e.getMessage());
        }
    }

    public UserResponse removeAvatarForUsername(String username) {
        User u = getUserEntityByUsername(username);
        if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
            String publicId = extractPublicIdFromUrl(u.getAvatarUrl());
            if (publicId != null) cloudinaryService.deleteResource(publicId);
        }
        u.setAvatarUrl(null);
        u.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(u);
        return userMapper.toUserResponse(u);
    }

    public UserResponse updateUser(Integer id, UserUpdateRequest request) {
    User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPassword() != null) user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    // Admin: list users with filters, search, and pagination
    public org.springframework.data.domain.Page<UserResponse> listUsersWithFilters(
            String q,
            Role role,
            Status status,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            int page,
            int size) {
        Specification<User> spec = Specification.where(UserSpecifications.searchByKeyword(q))
                .and(UserSpecifications.hasRole(role))
                .and(UserSpecifications.hasStatus(status))
                .and(UserSpecifications.createdBetween(createdFrom, createdTo));

        var pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        return userRepository.findAll(spec, pageable).map(userMapper::toUserResponse);
    }

    @Transactional
    public void banUser(Integer userId, String reason) {
        User u = getUserEntityById(userId);
        if (u.getStatus() == Status.BANNED) return;
        u.setStatus(Status.BANNED);
        u.setUpdatedAt(LocalDateTime.now());
        userRepository.save(u);
        if (u.getEmail() != null) {
            emailService.sendNotification(u.getEmail(), "Tài khoản bị khóa", "Tài khoản của bạn đã bị khóa. Lý do: " + (reason == null ? "" : reason));
        }
    }

    @Transactional
    public void unbanUser(Integer userId, String reason) {
        User u = getUserEntityById(userId);
        if (u.getStatus() != Status.BANNED) return;
        u.setStatus(Status.ACTIVE);
        u.setUpdatedAt(LocalDateTime.now());
        userRepository.save(u);
        if (u.getEmail() != null) {
            emailService.sendNotification(u.getEmail(), "Tài khoản được mở khóa", "Tài khoản của bạn đã được mở khóa. " + (reason == null ? "" : "Lý do: " + reason));
        }
    }

    @Transactional
    public UserResponse changeUserRole(Integer userId, Role newRole) {
        User u = getUserEntityById(userId);
        u.setRole(newRole);
        u.setUpdatedAt(LocalDateTime.now());
        userRepository.save(u);
        return userMapper.toUserResponse(u);
    }

    // Simple user activity: return last login, createdAt, updatedAt
    public java.util.Map<String, Object> getUserActivity(Integer userId) {
        User u = getUserEntityById(userId);
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("userId", u.getUserId());
        map.put("username", u.getUsername());
        map.put("lastLoginAt", u.getLastLoginAt());
        map.put("createdAt", u.getCreatedAt());
        map.put("updatedAt", u.getUpdatedAt());
        // For now include email verification
        map.put("emailVerifiedAt", u.getEmailVerifiedAt());
        return map;
    }

    // Request a password reset (sends OTP/token to email)
    @Transactional
    public void requestPasswordReset(String email) {
    var userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) throw new AppException(ErrorCode.NOT_FOUND, "Email does not exist");

        String token = generateToken();
        LocalDateTime expires = LocalDateTime.now().plusMinutes(10);

        // remove previous tokens for this email and type
        tokenRepository.deleteByEmailAndType(email, "PASSWORD_RESET");
        VerificationToken vt = VerificationToken.builder()
                .email(email)
                .token(token)
                .expiresAt(expires)
                .type("PASSWORD_RESET")
                .build();
        tokenRepository.save(vt);

        String body = "Mã OTP của bạn là: " + token;
        emailService.sendOtp(new MailBody(email, "BilliardShop - Yêu cầu đặt lại mật khẩu", body));
    }

    // Verify OTP and reset password
    @Transactional
    public void resetPassword(String email, String token, String newPassword) {
    VerificationToken vt = tokenRepository.findByTokenAndType(token, "PASSWORD_RESET")
        .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Invalid token"));
    if (!vt.getEmail().equals(email)) throw new AppException(ErrorCode.INVALID_REQUEST, "Token does not match email");
    if (vt.getExpiresAt().isBefore(LocalDateTime.now())) throw new AppException(ErrorCode.INVALID_DATE_RANGE, "Token has expired");

    var user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.deleteByEmailAndType(email, "PASSWORD_RESET");
        emailService.sendNotification(email, "Mật khẩu đã được thay đổi", "Mật khẩu tài khoản của bạn đã được thay đổi thành công.");
    }

    // Request email verification (sends OTP/token to email)
    @Transactional
    public void requestEmailVerification(String email) {
    var userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) throw new AppException(ErrorCode.NOT_FOUND, "Email does not exist");

        String token = generateToken();
        LocalDateTime expires = LocalDateTime.now().plusMinutes(10);

        tokenRepository.deleteByEmailAndType(email, "EMAIL_VERIFICATION");
        VerificationToken vt = VerificationToken.builder()
                .email(email)
                .token(token)
                .expiresAt(expires)
                .type("EMAIL_VERIFICATION")
                .build();
        tokenRepository.save(vt);

        String body = "Mã xác thực email của bạn là: " + token;
        emailService.sendOtp(new MailBody(email, "BilliardShop - Xác thực email", body));
    }

    @Transactional
    public void verifyEmail(String email, String token) {
    VerificationToken vt = tokenRepository.findByTokenAndType(token, "EMAIL_VERIFICATION")
        .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Invalid token"));
    if (!vt.getEmail().equals(email)) throw new AppException(ErrorCode.INVALID_REQUEST, "Token does not match email");
    if (vt.getExpiresAt().isBefore(LocalDateTime.now())) throw new AppException(ErrorCode.INVALID_DATE_RANGE, "Token has expired");

    var user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        tokenRepository.deleteByEmailAndType(email, "EMAIL_VERIFICATION");
        emailService.sendNotification(email, "Email đã được xác thực", "Cảm ơn bạn đã xác thực email.");
    }

    private String generateToken() {
        // simple numeric OTP of 6 digits
        int num = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(num);
    }

     /**
     * Upload avatar cho user
     * @param userId ID của user
     * @param avatarFile File ảnh avatar
     * @return UserResponse đã cập nhật
     */
     @Transactional
     public UserResponse uploadAvatar(Integer userId, MultipartFile avatarFile) {
     User user = userRepository.findById(userId)
         .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

         try {
             // Upload ảnh lên Cloudinary với folder "avatars" và preset "avatar"
             String avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars", "avatar");

             // Xóa avatar cũ nếu có (extract publicId từ URL)
             if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                 String oldPublicId = extractPublicIdFromUrl(user.getAvatarUrl());
                 if (oldPublicId != null) {
                     cloudinaryService.deleteResource(oldPublicId);
                 }
             }

             // Cập nhật URL avatar mới
             user.setAvatarUrl(avatarUrl);
             user.setUpdatedAt(LocalDateTime.now());
             userRepository.save(user);

             return userMapper.toUserResponse(user);

         } catch (IOException e) {
             throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to upload avatar: " + e.getMessage());
         }
     }

    /**
     * Xóa avatar của user
     * @param userId ID của user
     * @return UserResponse đã cập nhật
     */
    @Transactional
    public UserResponse removeAvatar(Integer userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        // Xóa avatar từ Cloudinary nếu có
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            String publicId = extractPublicIdFromUrl(user.getAvatarUrl());
            if (publicId != null) {
                cloudinaryService.deleteResource(publicId);
            }
        }

        // Xóa URL avatar
        user.setAvatarUrl(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    /**
     * Extract publicId từ Cloudinary URL
     * URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
     */
    private String extractPublicIdFromUrl(String url) {
        try {
            if (url == null || !url.contains("cloudinary.com")) {
                return null;
            }
            
            // Tìm vị trí của "/upload/"
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;
            
            // Lấy phần sau "/upload/"
            String afterUpload = url.substring(uploadIndex + 8);
            
            // Bỏ version nếu có (vXXXX/)
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                int slashIndex = afterUpload.indexOf("/");
                afterUpload = afterUpload.substring(slashIndex + 1);
            }
            
            // Bỏ extension (.jpg, .png, etc.)
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            
            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}
