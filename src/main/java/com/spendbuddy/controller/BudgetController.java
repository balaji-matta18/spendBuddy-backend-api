package com.spendbuddy.controller;

import com.spendbuddy.request.dto.BudgetRequest;
import com.spendbuddy.response.dto.BudgetResponse;
import com.spendbuddy.response.dto.BudgetSummaryResponse;
import com.spendbuddy.scheduler.MonthlyBudgetScheduler;
import com.spendbuddy.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;
    private final MonthlyBudgetScheduler monthlyBudgetScheduler;

    @Autowired
    public BudgetController(BudgetService budgetService, MonthlyBudgetScheduler monthlyBudgetScheduler) {
        this.budgetService = budgetService;
        this.monthlyBudgetScheduler = monthlyBudgetScheduler;
    }

    /**
     * ✅ Manual trigger for monthly budget rollover (useful for user confirmation or testing)
     */
    @PostMapping("/rollover")
    public ResponseEntity<String> triggerRollover() {
        monthlyBudgetScheduler.rolloverBudgets();
        return ResponseEntity.ok("✅ Manual rollover executed successfully!");
    }

    /**
     * ✅ Create or update if category already exists (for user’s current financial month)
     */
    @PostMapping
    public ResponseEntity<?> createOrUpdateBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequest request) {
        try {
            BudgetResponse resp = budgetService.save(userDetails, request);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Fetch budgets for the logged-in user
     * - Supports optional query param ?month=YYYY-MM
     * - If omitted, automatically uses user’s financial month based on monthStartDay.
     */
    @GetMapping("/all")
    public ResponseEntity<?> listBudgets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String month) {
        try {
            List<BudgetResponse> result;
            if (month != null && !month.isBlank()) {
                result = budgetService.listByMonth(userDetails, month);
                System.out.println("📊 Listing budgets for requested month: " + month);
            } else {
                result = budgetService.list(userDetails);
                System.out.println("📅 Listing budgets for user’s active financial month.");
            }

            if (result.isEmpty()) {
                return ResponseEntity.ok(List.of()); // Return empty list instead of null
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("⚠️ " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to fetch budgets. " + e.getMessage());
        }
    }

    /**
     * ✅ Dashboard summary (category + budget + spent)
     * Automatically uses financial month logic.
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<BudgetSummaryResponse> summary = budgetService.summary(userDetails);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to load budget summary: " + e.getMessage());
        }
    }

    /**
     * ✅ Update ONLY the budget amount (category name is locked)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudgetAmount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        try {
            BudgetResponse resp = budgetService.update(userDetails, id, request);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Delete budget by ID (only if owned by this user)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        try {
            budgetService.delete(userDetails, id);
            return ResponseEntity.ok("🗑️ Budget deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ " + e.getMessage());
        }
    }
}
