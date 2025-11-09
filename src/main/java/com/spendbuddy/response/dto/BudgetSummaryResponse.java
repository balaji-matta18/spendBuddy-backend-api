package com.spendbuddy.response.dto;

public class BudgetSummaryResponse {
    private String category;
    private Double budget;
    private double spent;
    private boolean overBudget;

    public BudgetSummaryResponse() {
    }

    // ✅ 4-argument constructor for full summary
    public BudgetSummaryResponse(String category, Double budget, double spent, boolean overBudget) {
        this.category = category;
        this.budget = budget;
        this.spent = spent;
        this.overBudget = overBudget;
    }

    // (Optional) if older 3-argument constructor was used somewhere else, keep it
    public BudgetSummaryResponse(String category, Double budget, double spent) {
        this(category, budget, spent, false);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public boolean isOverBudget() {
        return overBudget;
    }

    public void setOverBudget(boolean overBudget) {
        this.overBudget = overBudget;
    }
}
