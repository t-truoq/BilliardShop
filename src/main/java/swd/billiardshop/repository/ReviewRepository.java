package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
