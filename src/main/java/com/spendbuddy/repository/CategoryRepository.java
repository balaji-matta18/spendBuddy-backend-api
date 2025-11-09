package com.spendbuddy.repository;

import com.spendbuddy.entity.expensetracker.Category;
import com.spendbuddy.response.dto.CategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	// ✅ Check if category name exists for same user (UUID-based userId)
	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
			"FROM Category c WHERE c.name = :name AND c.user.id = :userId")
	boolean existsByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

	// ✅ Check if category exists by id and user
	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
			"FROM Category c WHERE c.id = :id AND c.user.id = :userId")
	boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	// ✅ List all categories for the logged-in user
//	@Query("SELECT new com.spendbuddy.response.dto.CategoryResponse(" +
//			"c.id, c.name, c.user.id, c.createdAt, c.updatedAt) " +
//			"FROM Category c WHERE c.user.id = :userId")
//	List<CategoryResponse> listCategory(@Param("userId") String userId);

	@Query("SELECT new com.spendbuddy.response.dto.CategoryResponse(" +
			"c.id, c.name, c.user.id, c.createdAt, c.updatedAt) " +
			"FROM Category c WHERE c.user.id = :userId")
	List<CategoryResponse> listCategory(@Param("userId") Long userId);

}
