package com.spendbuddy.entity.budget;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.expensetracker.Expense;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double budgetAmount;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "budget-expenses")
    private List<Expense> expenses;

    //  NEW: Adds month tracking for budgets
    @Column(name = "budget_month", length = 7)
    @Convert(converter = YearMonthAttributeConverter.class)
    private YearMonth budgetMonth = YearMonth.now();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (budgetMonth == null) budgetMonth = YearMonth.now(); // default to current month
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
