package com.spendbuddy.service;

import com.spendbuddy.response.dto.ReportResponse;
import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.expensetracker.Expense;
import com.spendbuddy.repository.ExpenseRepository;
import com.spendbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * 🥧 Expense by Category (uses Budget.category which is a String)
     */
    public List<ReportResponse> getExpenseByCategory(UserDetails userDetails, boolean lastSixMonths) {
        User user = getCurrentUser(userDetails);
        List<Expense> expenses = fetchExpensesForRange(user, lastSixMonths);

        Map<String, Double> grouped = expenses.stream()
                .filter(e -> e.getBudget() != null && e.getBudget().getCategory() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getBudget().getCategory(), //  Category is a String field in Budget
                        Collectors.summingDouble(Expense::getExpenseAmount)
                ));

        return mapToReportResponse(grouped);
    }

    /**
     * 📊 Monthly Spending Trend
     */
    public List<ReportResponse> getMonthlySummary(UserDetails userDetails, boolean lastSixMonths) {
        User user = getCurrentUser(userDetails);
        List<Expense> expenses = fetchExpensesForRange(user, lastSixMonths);

        Map<String, Double> grouped = expenses.stream()
                .filter(e -> e.getExpenseDate() != null)
                .collect(Collectors.groupingBy(
                        e -> String.format("%04d-%02d", e.getExpenseDate().getYear(), e.getExpenseDate().getMonthValue()),
                        Collectors.summingDouble(Expense::getExpenseAmount)
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    String label = java.time.Month.of(month).name().substring(0, 3) + " " + year;
                    return new ReportResponse(label, entry.getValue());
                })
                .collect(Collectors.toList());
    }

    /**
     * 💳 Payment Type Distribution
     */
    public List<ReportResponse> getPaymentTypeSummary(UserDetails userDetails, boolean lastSixMonths) {
        User user = getCurrentUser(userDetails);
        List<Expense> expenses = fetchExpensesForRange(user, lastSixMonths);

        Map<String, Double> grouped = expenses.stream()
                .filter(e -> e.getPayment() != null && e.getPayment().getType() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getPayment().getType(),
                        Collectors.summingDouble(Expense::getExpenseAmount)
                ));

        return mapToReportResponse(grouped);
    }

    // ---------- Helpers ----------

    private List<Expense> fetchExpensesForRange(User user, boolean lastSixMonths) {
        if (!lastSixMonths) {
            return expenseRepository.findByUser(user);
        }
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        LocalDate start = now.minusMonths(5).withDayOfMonth(1); // 6-month window
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());
        return expenseRepository.findByUserAndExpenseDateBetween(user, start, end);
    }

    private List<ReportResponse> mapToReportResponse(Map<String, Double> grouped) {
        return grouped.entrySet().stream()
                .map(entry -> new ReportResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
