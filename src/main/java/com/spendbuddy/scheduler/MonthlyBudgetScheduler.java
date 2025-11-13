package com.spendbuddy.scheduler;

import com.spendbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler now only checks/logs which users' financial month starts today.
 * It DOES NOT automatically copy budgets — user must trigger rollover manually.
 */
@Component
public class MonthlyBudgetScheduler {

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?") // runs daily at midnight
    public void rolloverBudgets() {
        LocalDate today = LocalDate.now();
        List<com.spendbuddy.entity.auth.User> users = userRepository.findAll();

        for (com.spendbuddy.entity.auth.User user : users) {
            int startDay = user.getMonthStartDay() != null ? user.getMonthStartDay() : 1;
            if (today.getDayOfMonth() == startDay) {
                System.out.printf("🔔 User '%s' financial month starts today (day %d)%n", user.getUsername(), startDay);
                // Important: we do NOT copy here. Manual rollover endpoint will copy when user confirms.
            }
        }

        System.out.println("✅ Daily financial-month check completed for " + today);
    }
}
