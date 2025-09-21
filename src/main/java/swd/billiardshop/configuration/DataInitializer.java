package swd.billiardshop.configuration;

import swd.billiardshop.entity.User;
import swd.billiardshop.enums.Gender;
import swd.billiardshop.enums.Role;
import swd.billiardshop.enums.Status;
import swd.billiardshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialAdminPassword;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${initial.admin.password}") String initialAdminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialAdminPassword = initialAdminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        // Không cần tạo entity Role, chỉ cần kiểm tra và tạo user admin với role là enum
        if (!userRepository.findByUsername("admin").isPresent()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPasswordHash(passwordEncoder.encode(initialAdminPassword));
            adminUser.setFullName("Admin User");
            adminUser.setPhone("0123456789");
            adminUser.setAvatarUrl(null);
            adminUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
            adminUser.setGender(Gender.MALE);
            adminUser.setRole(Role.ADMIN);
            adminUser.setStatus(Status.ACTIVE);
            userRepository.save(adminUser);
            System.out.println("Admin account created with username: admin and password: " + initialAdminPassword);
        } else {
            System.out.println("Admin account already exists.");
        }
    }
}
