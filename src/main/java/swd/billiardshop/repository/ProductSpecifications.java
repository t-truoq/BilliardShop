package swd.billiardshop.repository;

import org.springframework.data.jpa.domain.Specification;
import swd.billiardshop.entity.Product;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;

public class ProductSpecifications {
    public static Specification<Product> search(String q, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                                String brand, String material, Integer minRating, Boolean inStockOnly) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (q != null && !q.trim().isEmpty()) {
                String like = "%" + q.toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category").get("categoryId"), categoryId));
            }
            if (minPrice != null) predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (brand != null) predicate = cb.and(predicate, cb.equal(root.get("brand"), brand));
            if (material != null) predicate = cb.and(predicate, cb.equal(root.get("material"), material));
            if (minRating != null) predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            if (inStockOnly != null && inStockOnly) predicate = cb.and(predicate, cb.greaterThan(root.get("stockQuantity"), 0));
            return predicate;
        };
    }
}
