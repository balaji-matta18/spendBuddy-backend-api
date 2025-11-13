package com.spendbuddy.service;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.budget.Budget;
import com.spendbuddy.entity.expensetracker.Expense;
import com.spendbuddy.repository.BudgetRepository;
import com.spendbuddy.repository.ExpenseRepository;
import com.spendbuddy.repository.UserRepository;
import com.spendbuddy.response.dto.ExpenseResponse;
import com.spendbuddy.response.dto.StatsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * Format percentage difference as +xx.xx%
     */
    private String formatChange(double current, double previous) {
        if (previous == 0) return "+0.00%";
        double diff = ((current - previous) / previous) * 100.0;
        return String.format("%+.2f%%", diff);
    }

    /**
     * ✅ Get current dashboard statistics (respects user’s custom month start day)
     */
    public StatsResponse getStats(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        int monthStartDay = user.getMonthStartDay() != null ? user.getMonthStartDay() : 1;
        LocalDate today = LocalDate.now();

        // --- Calculate user's custom month range safely ---
        LocalDate calculatedStart = today.withDayOfMonth(
                Math.min(monthStartDay, today.lengthOfMonth())
        );

        if (today.getDayOfMonth() < monthStartDay) {
            calculatedStart = calculatedStart.minusMonths(1);
        }

        final LocalDate startOfMonth = calculatedStart;
        final LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        // --- Previous month range based on same logic ---
        final LocalDate prevEnd = startOfMonth.minusDays(1);
        final LocalDate prevStart = prevEnd.minusMonths(1).plusDays(1);

        YearMonth currentYm = YearMonth.from(startOfMonth);
        YearMonth prevYm = currentYm.minusMonths(1);

        int daysElapsed = (int) (today.toEpochDay() - startOfMonth.toEpochDay()) + 1;
        int daysInPrevMonth = (int) (prevEnd.toEpochDay() - prevStart.toEpochDay()) + 1;

        // --- Fetch budgets for current and previous months ---
        List<Budget> currentBudgets = budgetRepository.findByUserAndBudgetMonth(user, currentYm);
        List<Budget> prevBudgets = budgetRepository.findByUserAndBudgetMonth(user, prevYm);

        // --- Calculate total budget amounts ---
        double totalBudget = currentBudgets.stream()
                .filter(b -> b.getBudgetAmount() != null)
                .mapToDouble(Budget::getBudgetAmount)
                .sum();

        double prevTotalBudget = prevBudgets.stream()
                .filter(b -> b.getBudgetAmount() != null)
                .mapToDouble(Budget::getBudgetAmount)
                .sum();

        // --- Fetch user's expenses ---
        List<Expense> allExpenses = expenseRepository.findByUser(user);

        // --- Current month expenses ---
        double currentExpenses = allExpenses.stream()
                .filter(e -> e.getExpenseDate() != null)
                .filter(e -> !e.getExpenseDate().isBefore(startOfMonth) && !e.getExpenseDate().isAfter(endOfMonth))
                .mapToDouble(e -> e.getExpenseAmount() == null ? 0.0 : e.getExpenseAmount())
                .sum();

        // --- Previous month expenses ---
        double prevExpenses = allExpenses.stream()
                .filter(e -> e.getExpenseDate() != null)
                .filter(e -> !e.getExpenseDate().isBefore(prevStart) && !e.getExpenseDate().isAfter(prevEnd))
                .mapToDouble(e -> e.getExpenseAmount() == null ? 0.0 : e.getExpenseAmount())
                .sum();

        // --- Average Daily Spendings ---
        double avgDaily = daysElapsed > 0 ? (currentExpenses / daysElapsed) : 0.0;
        double avgPrevDaily = daysInPrevMonth > 0 ? (prevExpenses / daysInPrevMonth) : 0.0;

        // --- Savings ---
        double savings = totalBudget - currentExpenses;
        double prevSavings = prevTotalBudget - prevExpenses;

        // --- Percentage changes ---
        String balanceChange = formatChange(totalBudget, prevTotalBudget);
        String expensesChange = formatChange(currentExpenses, prevExpenses);
        String spendingsChange = formatChange(avgDaily, avgPrevDaily);
        String savingsChange = formatChange(savings, prevSavings);

        // --- Build Response ---
        StatsResponse stats = new StatsResponse();
        stats.setTotalBalance(totalBudget);
        stats.setExpenses(currentExpenses);
        stats.setAvgDailySpending(avgDaily);
        stats.setSavings(savings);

        stats.setBalanceChange(balanceChange);
        stats.setExpensesChange(expensesChange);
        stats.setSpendingsChange(spendingsChange);
        stats.setSavingsChange(savingsChange);

        return stats;
    }

    /**
     * ✅ Return last 5 recent transactions
     */
    public List<ExpenseResponse> getRecentTransactions(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return expenseRepository.findByUser(user)
                .stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return 0;
                })
                .map(ExpenseResponse::fromEntity)
                .limit(5)
                .collect(Collectors.toList());
    }
}
