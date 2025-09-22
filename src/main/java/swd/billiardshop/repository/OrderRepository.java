package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Order;
import swd.billiardshop.entity.User;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Integer> {
	List<Order> findByUser(User user);


	boolean existsByAddress_AddressIdAndUser(Integer addressId, User user);
}
