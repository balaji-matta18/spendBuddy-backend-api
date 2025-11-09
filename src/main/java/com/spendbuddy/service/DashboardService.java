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
     * Get current dashboard statistics for the logged-in user
     */
    public StatsResponse getStats(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // --- Total Budget ---
        List<Budget> budgets = budgetRepository.findByUser(user);
        double totalBudget = budgets.stream()
                .filter(b -> b.getBudgetAmount() != null)
                .mapToDouble(Budget::getBudgetAmount)
                .sum();

        // --- All Expenses ---
        List<Expense> allExpenses = expenseRepository.findByUser(user);

        // --- Current and Previous Month Ranges ---
        LocalDate today = LocalDate.now();
        YearMonth currentYm = YearMonth.from(today);
        YearMonth prevYm = currentYm.minusMonths(1);

        LocalDate startOfMonth = currentYm.atDay(1);
        LocalDate endOfMonth = currentYm.atEndOfMonth();
        LocalDate prevStart = prevYm.atDay(1);
        LocalDate prevEnd = prevYm.atEndOfMonth();

        int daysElapsed = today.getDayOfMonth();
        int daysInPrevMonth = prevYm.lengthOfMonth();

        // --- Current Month Expenses ---
        double currentExpenses = allExpenses.stream()
                .filter(e -> e.getExpenseDate() != null)
                .filter(e -> {
                    LocalDate date = e.getExpenseDate();
                    // Inclusive comparison for current month
                    return (date.isAfter(startOfMonth.minusDays(1)) && date.isBefore(endOfMonth.plusDays(1)));
                })
                .mapToDouble(e -> e.getExpenseAmount() == null ? 0.0 : e.getExpenseAmount())
                .sum();

        // --- Previous Month Expenses (FIXED FILTERING) ---
        double prevExpenses = allExpenses.stream()
                .filter(e -> e.getExpenseDate() != null)
                .filter(e -> {
                    LocalDate date = e.getExpenseDate();
                    // Inclusive comparison for previous month
                    return (date.isAfter(prevStart.minusDays(1)) && date.isBefore(prevEnd.plusDays(1)));
                })
                .mapToDouble(e -> e.getExpenseAmount() == null ? 0.0 : e.getExpenseAmount())
                .sum();

        // --- Average Daily Spendings ---
        double avgDaily = daysElapsed > 0 ? (currentExpenses / daysElapsed) : 0.0;
        double avgPrevDaily = daysInPrevMonth > 0 ? (prevExpenses / daysInPrevMonth) : 0.0;

        // --- Savings ---
        double savings = totalBudget - currentExpenses;

        // --- Previous Total Budget (up to prev month end) ---
        double prevTotalBudget = budgets.stream()
                .filter(b -> {
                    if (b.getCreatedAt() == null) return true;
                    return !b.getCreatedAt().toLocalDate().isAfter(prevEnd);
                })
                .filter(b -> b.getBudgetAmount() != null)
                .mapToDouble(Budget::getBudgetAmount)
                .sum();

        double prevSavings = prevTotalBudget - prevExpenses;

        // --- Calculate % Changes ---
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
     * Return last 5 recent transactions
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
