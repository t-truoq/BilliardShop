package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.ProductImage;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductProductIdOrderBySortOrderAsc(Integer productId);
    Integer countByProductProductId(Integer productId);
}
