package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
}
