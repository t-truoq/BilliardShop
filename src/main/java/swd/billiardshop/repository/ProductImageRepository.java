package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
}
