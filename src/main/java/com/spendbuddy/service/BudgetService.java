package com.spendbuddy.service;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.budget.Budget;
import com.spendbuddy.repository.BudgetRepository;
import com.spendbuddy.repository.UserRepository;
import com.spendbuddy.request.dto.BudgetRequest;
import com.spendbuddy.response.dto.BudgetResponse;
import com.spendbuddy.response.dto.BudgetSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Autowired(required = false)
    private ExpenseService expenseService;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    // Determine effective financial month based on user's monthStartDay
    private YearMonth getEffectiveBudgetMonth(User user) {
        int startDay = user.getMonthStartDay() != null ? user.getMonthStartDay() : 1;
        LocalDate today = LocalDate.now();

        if (today.getDayOfMonth() < startDay) {
            today = today.minusMonths(1);
        }

        return YearMonth.of(today.getYear(), today.getMonth());
    }

    @Transactional
    public BudgetResponse save(UserDetails userDetails, BudgetRequest request) throws Exception {
        User user = resolveManagedUser(userDetails);
        YearMonth currentMonth = getEffectiveBudgetMonth(user);

        return budgetRepository.findByUserAndCategoryAndBudgetMonth(user, request.getCategory(), currentMonth)
                .map(existing -> {
                    existing.setBudgetAmount(request.getBudgetAmount());
                    Budget saved = budgetRepository.save(existing);
                    return new BudgetResponse(
                            saved.getId(),
                            saved.getCategory(),
                            saved.getBudgetAmount(),
                            saved.getBudgetMonth() != null ? saved.getBudgetMonth().toString() : "N/A"
                    );
                })
                .orElseGet(() -> {
                    Budget newBudget = new Budget();
                    newBudget.setUser(user);
                    newBudget.setCategory(request.getCategory());
                    newBudget.setBudgetAmount(request.getBudgetAmount());
                    newBudget.setBudgetMonth(currentMonth);
                    Budget saved = budgetRepository.save(newBudget);
                    return new BudgetResponse(
                            saved.getId(),
                            saved.getCategory(),
                            saved.getBudgetAmount(),
                            saved.getBudgetMonth() != null ? saved.getBudgetMonth().toString() : "N/A"
                    );
                });
    }

    public List<BudgetResponse> list(UserDetails userDetails) {
        User user = resolveManagedUser(userDetails);
        YearMonth currentMonth = getEffectiveBudgetMonth(user);

        List<Budget> list = budgetRepository.findByUserAndBudgetMonth(user, currentMonth);
        return list.stream()
                .map(b -> new BudgetResponse(
                        b.getId(),
                        b.getCategory(),
                        b.getBudgetAmount(),
                        b.getBudgetMonth() != null ? b.getBudgetMonth().toString() : "N/A"
                ))
                .collect(Collectors.toList());
    }

    public List<BudgetResponse> listByMonth(UserDetails userDetails, String month) {
        User user = resolveManagedUser(userDetails);

        YearMonth targetMonth;
        try {
            targetMonth = YearMonth.parse(month);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid month format. Expected format: YYYY-MM");
        }

        List<Budget> list = budgetRepository.findByUserAndBudgetMonth(user, targetMonth);
        return list.stream()
                .map(b -> new BudgetResponse(
                        b.getId(),
                        b.getCategory(),
                        b.getBudgetAmount(),
                        b.getBudgetMonth() != null ? b.getBudgetMonth().toString() : "N/A"
                ))
                .collect(Collectors.toList());
    }

    public List<BudgetSummaryResponse> summary(UserDetails userDetails) {
        User user = resolveManagedUser(userDetails);
        YearMonth currentMonth = getEffectiveBudgetMonth(user);

        List<Budget> list = budgetRepository.findByUserAndBudgetMonth(user, currentMonth);

        return list.stream().map(b -> {
            double spent = 0.0;
            if (expenseService != null) {
                try {
                    spent = expenseService.getSpentForBudget(userDetails, b);
                } catch (Exception ignored) {
                    spent = 0.0;
                }
            }
            boolean overBudget = b.getBudgetAmount() != null && spent > b.getBudgetAmount();
            return new BudgetSummaryResponse(b.getCategory(), b.getBudgetAmount(), spent, overBudget);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void delete(UserDetails userDetails, Long id) throws Exception {
        User user = resolveManagedUser(userDetails);
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new Exception("Budget not found or not owned by user"));
        budgetRepository.delete(budget);
    }

    @Transactional
    public BudgetResponse update(UserDetails userDetails, Long id, BudgetRequest request) throws Exception {
        User user = resolveManagedUser(userDetails);
        YearMonth currentMonth = getEffectiveBudgetMonth(user);

        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new Exception("Budget not found or not owned by user"));

        if (request.getBudgetAmount() != null) {
            budget.setBudgetAmount(request.getBudgetAmount());
        }

        if (budget.getBudgetMonth() == null) {
            budget.setBudgetMonth(currentMonth);
        }

        Budget saved = budgetRepository.save(budget);
        return new BudgetResponse(
                saved.getId(),
                saved.getCategory(),
                saved.getBudgetAmount(),
                saved.getBudgetMonth() != null ? saved.getBudgetMonth().toString() : "N/A"
        );
    }

    /**
     * Manual rollover for the currently authenticated user.
     * Copies budgets from previous financial month → user's current financial month
     * but only creates new records for categories that don't already exist for the target month.
     *
     * Returns number of budgets copied.
     */
    @Transactional
    public int rolloverForCurrentUser(UserDetails userDetails) throws Exception {
        User user = resolveManagedUser(userDetails);
        YearMonth currentMonth = getEffectiveBudgetMonth(user);
        YearMonth prevMonth = currentMonth.minusMonths(1);

        List<Budget> prevBudgets = budgetRepository.findByUserAndBudgetMonth(user, prevMonth);
        int copied = 0;

        for (Budget prev : prevBudgets) {
            boolean exists = budgetRepository
                    .findByUserAndCategoryAndBudgetMonth(user, prev.getCategory(), currentMonth)
                    .isPresent();

            if (!exists) {
                Budget newBudget = new Budget();
                newBudget.setUser(user);
                newBudget.setCategory(prev.getCategory());
                newBudget.setBudgetAmount(prev.getBudgetAmount());
                newBudget.setBudgetMonth(currentMonth);
                budgetRepository.save(newBudget);
                copied++;
            }
        }

        return copied;
    }

    private User resolveManagedUser(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalStateException("No authenticated user available");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in DB: " + userDetails.getUsername()));
    }
}
