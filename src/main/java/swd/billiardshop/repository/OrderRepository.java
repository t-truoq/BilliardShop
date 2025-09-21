package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
