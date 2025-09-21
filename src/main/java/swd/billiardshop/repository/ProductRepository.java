package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
