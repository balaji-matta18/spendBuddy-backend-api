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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Manual trigger for monthly budget rollover (copies previous financial month budgets
     * to current financial month for the authenticated user). Returns JSON with count.
     */
    @PostMapping("/rollover")
    public ResponseEntity<?> triggerRollover(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            int copied = budgetService.rolloverForCurrentUser(userDetails);
            Map<String, Object> resp = new HashMap<>();
            resp.put("copied", copied);
            resp.put("message", copied > 0 ? "Copied " + copied + " budgets." : "Nothing to copy.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrUpdateBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequest request) {
        try {
            BudgetResponse resp = budgetService.save(userDetails, request);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> listBudgets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String month) {
        try {
            List<BudgetResponse> result;
            if (month != null && !month.isBlank()) {
                result = budgetService.listByMonth(userDetails, month);
            } else {
                result = budgetService.list(userDetails);
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<BudgetSummaryResponse> summary = budgetService.summary(userDetails);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load budget summary: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudgetAmount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        try {
            BudgetResponse resp = budgetService.update(userDetails, id, request);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        try {
            budgetService.delete(userDetails, id);
            return ResponseEntity.ok(Map.of("message", "Budget deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
