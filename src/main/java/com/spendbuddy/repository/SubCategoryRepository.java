package com.spendbuddy.repository;

import com.spendbuddy.entity.expensetracker.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

	/**
	 * Return all subcategories for a budget that belong to given user.
	 * Used by SubCategoryService.listByBudget(user, budgetId).
	 */
	@Query("SELECT sc FROM SubCategory sc " +
			"WHERE sc.budget.id = :budgetId " +
			"  AND sc.budget.user.id = :userId " +
			"ORDER BY sc.id")
	List<SubCategory> listSubCategory(@Param("budgetId") Long budgetId, @Param("userId") Long userId);

	// (optional) helper methods you may want later:
	// boolean existsByIdAndBudgetId(Long id, Long budgetId);
	// List<SubCategory> findByBudgetId(Long budgetId);
}
