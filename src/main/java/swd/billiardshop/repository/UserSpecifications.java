package swd.billiardshop.repository;

import org.springframework.data.jpa.domain.Specification;
import swd.billiardshop.entity.User;
import swd.billiardshop.enums.Role;
import swd.billiardshop.enums.Status;

import java.time.LocalDateTime;

public final class UserSpecifications {
    private UserSpecifications() {}

    public static Specification<User> searchByKeyword(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return null;
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("phone")), like)
            );
        };
    }

    public static Specification<User> hasRole(Role role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(Status status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<User> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}
