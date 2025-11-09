package com.spendbuddy.response.dto;

public class BudgetResponse {
    private Long id;
    private String category;
    private Double budgetAmount;

    public BudgetResponse() {}

    public BudgetResponse(Long id, String category, Double budgetAmount) {
        this.id = id;
        this.category = category;
        this.budgetAmount = budgetAmount;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
}
