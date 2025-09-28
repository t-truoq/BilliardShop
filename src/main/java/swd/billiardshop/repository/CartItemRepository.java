package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
	List<CartItem> findByCartCartIdOrderByAddedAtDesc(Integer cartId);
	Optional<CartItem> findByCartCartIdAndProductProductId(Integer cartId, Integer productId);
	void deleteByCartCartId(Integer cartId);
	void deleteByCartCartIdAndProductProductId(Integer cartId, Integer productId);
	int countByCartCartId(Integer cartId);
}
