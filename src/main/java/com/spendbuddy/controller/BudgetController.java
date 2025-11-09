package com.spendbuddy.controller;

import com.spendbuddy.request.dto.BudgetRequest;
import com.spendbuddy.response.dto.BudgetResponse;
import com.spendbuddy.response.dto.BudgetSummaryResponse;
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

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // ✅ Create or update if category already exists
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

    // ✅ Fetch all budgets for the logged-in user
    @GetMapping("/all")
    public ResponseEntity<List<BudgetResponse>> listBudgets(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(budgetService.list(userDetails));
    }

    // ✅ Dashboard summary (category + budget + spent)
    @GetMapping("/summary")
    public ResponseEntity<List<BudgetSummaryResponse>> getSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(budgetService.summary(userDetails));
    }

    // ✅ Update ONLY budget amount (category is locked)
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

    // ✅ Delete budget by ID (only if owned by the user)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        try {
            budgetService.delete(userDetails, id);
            return ResponseEntity.noContent().build(); // returns 204 (no content)
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
