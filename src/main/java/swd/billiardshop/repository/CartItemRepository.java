package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
}
