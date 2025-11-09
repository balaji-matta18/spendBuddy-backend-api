package com.spendbuddy.repository;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.budget.Budget;
import com.spendbuddy.entity.expensetracker.Expense;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	// Simple, type-safe entity queries
	List<Expense> findByBudget(Budget budget);

	List<Expense> findByUser(User user);

	List<Expense> findByUserAndExpenseDateBetween(User user, LocalDate from, LocalDate to);

	List<Expense> findByBudgetAndUser(Budget budget, User user);

	List<Expense> findByBudgetAndUserAndExpenseDateBetween(Budget budget, User user, LocalDate from, LocalDate to);

	@Transactional
	void deleteByIdAndUserId(Long expenseId, Long userId);

	// ✅ Fixed version of your custom summary query
	@Query("""
        SELECT SUM(e.expenseAmount)
        FROM Expense e
        WHERE e.user.id = :userId
          AND e.budget.category = :budgetName
          AND e.expenseDate BETWEEN :startDate AND :endDate
    """)
	Double sumExpenseAmountByBudgetAndUserAndDateRange(
			@Param("userId") Long userId,
			@Param("budgetName") String budgetName,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate
	);
}
