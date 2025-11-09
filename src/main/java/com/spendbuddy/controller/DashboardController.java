package com.spendbuddy.controller;

import com.spendbuddy.response.dto.StatsResponse;
import com.spendbuddy.response.dto.ExpenseResponse;
import com.spendbuddy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // Dashboard summary for current month (Total Balance, Income, Expenses, Savings)
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        StatsResponse stats = dashboardService.getStats(userDetails);
        return ResponseEntity.ok(stats);
    }

    // Recent 5 transactions for the user
    @GetMapping("/recent")
    public ResponseEntity<List<ExpenseResponse>> recentTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        List<ExpenseResponse> recent = dashboardService.getRecentTransactions(userDetails);
        return ResponseEntity.ok(recent);
    }
}
