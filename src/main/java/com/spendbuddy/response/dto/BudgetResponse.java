package com.spendbuddy.response.dto;

public class BudgetResponse {
    private Long id;
    private String category;
    private Double budgetAmount;
    private String budgetMonth; // ✅ New field added

    public BudgetResponse() {}

    public BudgetResponse(Long id, String category, Double budgetAmount, String budgetMonth) {
        this.id = id;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.budgetMonth = budgetMonth;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public String getBudgetMonth() {
        return budgetMonth;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public void setBudgetMonth(String budgetMonth) {
        this.budgetMonth = budgetMonth;
    }
}
