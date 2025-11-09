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

    @Transactional
    public BudgetResponse save(UserDetails userDetails, BudgetRequest request) throws Exception {
        User user = resolveManagedUser(userDetails);

        // If budget for same user & category exists -> update it instead of creating new
        return budgetRepository.findByUserAndCategory(user, request.getCategory())
                .map(existing -> {
                    existing.setBudgetAmount(request.getBudgetAmount());
                    Budget saved = budgetRepository.save(existing);
                    return new BudgetResponse(saved.getId(), saved.getCategory(), saved.getBudgetAmount());
                })
                .orElseGet(() -> {
                    Budget newBudget = new Budget();
                    newBudget.setUser(user);
                    newBudget.setCategory(request.getCategory());
                    newBudget.setBudgetAmount(request.getBudgetAmount());
                    Budget saved = budgetRepository.save(newBudget);
                    return new BudgetResponse(saved.getId(), saved.getCategory(), saved.getBudgetAmount());
                });
    }

    public List<BudgetResponse> list(UserDetails userDetails) {
        User user = resolveManagedUser(userDetails);
        List<Budget> list = budgetRepository.findByUser(user);
        return list.stream()
                .map(b -> new BudgetResponse(b.getId(), b.getCategory(), b.getBudgetAmount()))
                .collect(Collectors.toList());
    }

//    public List<BudgetSummaryResponse> summary(UserDetails userDetails) {
//        User user = resolveManagedUser(userDetails);
//        List<Budget> list = budgetRepository.findByUser(user);
//
//        return list.stream().map(b -> {
//            double spent = 0.0;
//            if (expenseService != null) {
//                try {
////                    spent = expenseService.getSpentForCategory(userDetails, b.getCategory());
//                    spent = expenseService.getSpentForBudget(userDetails, b);
//
//                } catch (Exception ignored) {
//                    spent = 0.0;
//                }
//            }
//            boolean overBudget = b.getBudgetAmount() != null && spent > b.getBudgetAmount();
//            return new BudgetSummaryResponse(b.getCategory(), b.getBudgetAmount(), spent, overBudget);
//        }).collect(Collectors.toList());
//    }


public List<BudgetSummaryResponse> summary(UserDetails userDetails) {
    User user = resolveManagedUser(userDetails);
    List<Budget> list = budgetRepository.findByUser(user);

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

        // ✅ Ensure the budget belongs to the logged-in user
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new Exception("Budget not found or not owned by user"));

        // ✅ Only update budget amount, not category
        if (request.getBudgetAmount() != null) {
            budget.setBudgetAmount(request.getBudgetAmount());
        }

        Budget saved = budgetRepository.save(budget);
        return new BudgetResponse(saved.getId(), saved.getCategory(), saved.getBudgetAmount());
    }

    // Helper: load managed User entity
    private User resolveManagedUser(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalStateException("No authenticated user available");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in DB: " + userDetails.getUsername()));
    }
}
