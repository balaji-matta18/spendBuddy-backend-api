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

    /**
     * ✅ Determine the effective financial month based on the user’s preferred start day.
     * Example: if monthStartDay = 5 and today is 3 Nov → still belongs to October’s budget cycle.
     */
//    private YearMonth getEffectiveBudgetMonth(User user) {
//        int startDay = user.getMonthStartDay() != null ? user.getMonthStartDay() : 1;
//        LocalDate today = LocalDate.now();
//
//        // If today's date is before the start day, consider it as the previous financial month
//        if (today.getDayOfMonth() < startDay) {
//            today = today.minusMonths(1);
//        }
//
//        YearMonth effectiveMonth = YearMonth.of(today.getYear(), today.getMonth());
//        System.out.println("📅 Effective financial month for user '" + user.getUsername() + "' → " + effectiveMonth);
//        return effectiveMonth;
//    }

    private YearMonth getEffectiveBudgetMonth(User user) {
        int startDay = user.getMonthStartDay() != null ? user.getMonthStartDay() : 1;
        LocalDate today = LocalDate.now();

        // 👉 If today is *on or after* the start day → it's the *new* month.
        // If today is *before* the start day → still part of previous month.
        if (today.getDayOfMonth() < startDay) {
            today = today.minusMonths(1);
        }

        return YearMonth.of(today.getYear(), today.getMonth());
    }

    /**
     * ✅ Create or update a budget for the user’s current financial month.
     */
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

    /**
     * ✅ List budgets for the user’s active financial month.
     */
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

    /**
     * ✅ Fetch budgets for a specific month if provided (?month=YYYY-MM).
     */
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

    /**
     * ✅ Monthly summary based on the user's active financial cycle.
     */
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

    /**
     * ✅ Delete a budget if it belongs to this user (any financial month).
     */
    @Transactional
    public void delete(UserDetails userDetails, Long id) throws Exception {
        User user = resolveManagedUser(userDetails);
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new Exception("Budget not found or not owned by user"));
        budgetRepository.delete(budget);
    }

    /**
     * ✅ Update an existing budget for the user’s current financial month.
     */
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
     * ✅ Helper: Get the currently authenticated and persisted user entity.
     */
    private User resolveManagedUser(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalStateException("No authenticated user available");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in DB: " + userDetails.getUsername()));
    }
}
