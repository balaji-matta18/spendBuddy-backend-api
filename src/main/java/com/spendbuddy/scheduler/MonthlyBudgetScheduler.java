package com.spendbuddy.scheduler;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.budget.Budget;
import com.spendbuddy.repository.BudgetRepository;
import com.spendbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * ✅ Handles monthly budget rollover logic — both automatic (scheduler) and manual (triggered from controller)
 */
@Component
public class MonthlyBudgetScheduler {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * ✅ Automatically runs every day at midnight
     * to detect users whose month starts today.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledRollover() {
        rolloverBudgets(); // simply call the same method below
    }

    /**
     * ✅ Manual or reusable rollover logic (called from Controller)
     */
    public void rolloverBudgets() {
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            int startDay = user.getMonthStartDay() != null ? user.getMonthStartDay() : 1;

            if (today.getDayOfMonth() != startDay) continue;

            YearMonth currentMonth = getEffectiveMonthForUser(user, today);
            YearMonth prevMonth = currentMonth.minusMonths(1);

            List<Budget> prevBudgets = budgetRepository.findByUserAndBudgetMonth(user, prevMonth);
            if (prevBudgets.isEmpty()) continue;

            for (Budget prev : prevBudgets) {
                boolean exists = budgetRepository
                        .findByUserAndCategoryAndBudgetMonth(user, prev.getCategory(), currentMonth)
                        .isPresent();

                if (!exists) {
                    Budget newBudget = new Budget();
                    newBudget.setUser(user);
                    newBudget.setCategory(prev.getCategory());
                    newBudget.setBudgetAmount(prev.getBudgetAmount());
                    newBudget.setBudgetMonth(currentMonth);

                    budgetRepository.save(newBudget);
                    System.out.printf("📦 Copied '%s' for %s (%s → %s)%n",
                            prev.getCategory(), user.getUsername(), prevMonth, currentMonth);
                }
            }
        }

        System.out.println("✅ Rollover check complete for " + today);
    }

    /**
     * Helper: Determine the correct effective financial month.
     */
    private YearMonth getEffectiveMonthForUser(User user, LocalDate today) {
        int startDay = user.getMonthStartDay() != null ? user.getMonthStartDay() : 1;

        if (today.getDayOfMonth() < startDay) {
            today = today.minusMonths(1);
        }

        return YearMonth.of(today.getYear(), today.getMonth());
    }
}
