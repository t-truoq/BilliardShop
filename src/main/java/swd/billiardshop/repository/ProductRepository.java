package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import swd.billiardshop.entity.Product;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
	@Query("select count(p) from Product p where p.category.categoryId = :categoryId")
	Integer countByCategoryId(@Param("categoryId") Integer categoryId);

	boolean existsBySku(String sku);
	boolean existsBySlug(String slug);
}
