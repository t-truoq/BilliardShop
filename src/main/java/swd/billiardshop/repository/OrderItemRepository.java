package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.OrderItem;
import swd.billiardshop.entity.Order;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
	List<OrderItem> findByOrder(Order order);
}
