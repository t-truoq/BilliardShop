package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
