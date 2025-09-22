package swd.billiardshop.service;

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
import java.time.LocalDateTime;
import swd.billiardshop.configuration.JwtUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;

    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent() ||
            userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Username or email already exists");
        }
        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public String login(UserLoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }
        User user = userOpt.get();
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getUserId());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(Integer id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPassword() != null) user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    // Request a password reset (sends OTP/token to email)
    @Transactional
    public void requestPasswordReset(String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) throw new RuntimeException("Email không tồn tại");

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
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));
        if (!vt.getEmail().equals(email)) throw new RuntimeException("Token không khớp email");
        if (vt.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("Token đã hết hạn");

        var user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.deleteByEmailAndType(email, "PASSWORD_RESET");
        emailService.sendNotification(email, "Mật khẩu đã được thay đổi", "Mật khẩu tài khoản của bạn đã được thay đổi thành công.");
    }

    // Request email verification (sends OTP/token to email)
    @Transactional
    public void requestEmailVerification(String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) throw new RuntimeException("Email không tồn tại");

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
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));
        if (!vt.getEmail().equals(email)) throw new RuntimeException("Token không khớp email");
        if (vt.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("Token đã hết hạn");

        var user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
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
}
