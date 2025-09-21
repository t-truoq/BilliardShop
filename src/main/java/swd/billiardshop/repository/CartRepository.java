package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
}
