package com.spendbuddy.request.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BudgetRequest {

    @NotBlank(message = "Category name cannot be blank")
    private String category;

    @NotNull(message = "Budget amount cannot be null")
    @Min(value = 0, message = "Budget amount must be a positive number")
    private Double budgetAmount;

    public BudgetRequest() {
    }

    public BudgetRequest(String category, Double budgetAmount) {
        this.category = category;
        this.budgetAmount = budgetAmount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
}
