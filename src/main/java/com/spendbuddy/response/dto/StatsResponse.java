package com.spendbuddy.response.dto;

/**
 * Dashboard statistics response DTO
 * Final shape:
 * {
 *   "totalBalance": 20000,
 *   "expenses": 12000,
 *   "avgDailySpending": 400,
 *   "savings": 8000,
 *   "balanceChange": "+0.00%",
 *   "expensesChange": "+0.00%",
 *   "spendingsChange": "+0.00%",
 *   "savingsChange": "+0.00%"
 * }
 */
public class StatsResponse {
    private double totalBalance;
    private double expenses;
    private double avgDailySpending;
    private double savings;

    // change fields aligned with requested metrics
    private String balanceChange;
    private String expensesChange;
    private String spendingsChange;
    private String savingsChange;

    public StatsResponse() {}

    public StatsResponse(double totalBalance, double expenses, double avgDailySpending, double savings) {
        this.totalBalance = totalBalance;
        this.expenses = expenses;
        this.avgDailySpending = avgDailySpending;
        this.savings = savings;
        this.balanceChange = "+0.00%";
        this.expensesChange = "+0.00%";
        this.spendingsChange = "+0.00%";
        this.savingsChange = "+0.00%";
    }

    public double getTotalBalance() { return totalBalance; }
    public void setTotalBalance(double totalBalance) { this.totalBalance = totalBalance; }

    public double getExpenses() { return expenses; }
    public void setExpenses(double expenses) { this.expenses = expenses; }

    public double getAvgDailySpending() { return avgDailySpending; }
    public void setAvgDailySpending(double avgDailySpending) { this.avgDailySpending = avgDailySpending; }

    public double getSavings() { return savings; }
    public void setSavings(double savings) { this.savings = savings; }

    public String getBalanceChange() { return balanceChange; }
    public void setBalanceChange(String balanceChange) { this.balanceChange = balanceChange; }

    public String getExpensesChange() { return expensesChange; }
    public void setExpensesChange(String expensesChange) { this.expensesChange = expensesChange; }

    public String getSpendingsChange() { return spendingsChange; }
    public void setSpendingsChange(String spendingsChange) { this.spendingsChange = spendingsChange; }

    public String getSavingsChange() { return savingsChange; }
    public void setSavingsChange(String savingsChange) { this.savingsChange = savingsChange; }
}
