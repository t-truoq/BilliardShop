package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.VerificationToken;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, String type);
    Optional<VerificationToken> findByEmailAndType(String email, String type);
    void deleteByEmailAndType(String email, String type);
}
