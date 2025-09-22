package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.billiardshop.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	Optional<Category> findBySlug(String slug);

	boolean existsByNameAndParent_Name(String name, String parentName);

	@Query("select c from Category c where c.parent.categoryId = :parentId")
	List<Category> findByParentId(@Param("parentId") Integer parentId);

	@Query("select count(p) from Product p where p.category.categoryId = :categoryId")
	Integer countProductsInCategory(@Param("categoryId") Integer categoryId);
}
