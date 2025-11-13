package com.spendbuddy.repository;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.budget.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUser(User user);


    Optional<Budget> findByUserAndCategory(User user, String category);

    Optional<Budget> findByIdAndUser(Long id, User user);

    void deleteByIdAndUser(Long id, User user);

    /**
     *  Custom query to check if a budget belongs to a specific user
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Budget b WHERE b.id = :budgetId AND b.user = :user")
    boolean existsByIdAndUser(@Param("budgetId") Long budgetId, @Param("user") User user);

    //  NEW: Find all budgets for a specific month and user
    List<Budget> findByUserAndBudgetMonth(User user, YearMonth budgetMonth);

    //  NEW: Find a specific category's budget for a given month
    Optional<Budget> findByUserAndCategoryAndBudgetMonth(User user, String category, YearMonth budgetMonth);
}
