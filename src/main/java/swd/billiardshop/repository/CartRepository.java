package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
	Optional<Cart> findByUserUserId(Integer userId);
	void deleteByUserUserId(Integer userId);
}
