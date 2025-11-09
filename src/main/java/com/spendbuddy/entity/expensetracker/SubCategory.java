package com.spendbuddy.entity.expensetracker;

import com.spendbuddy.entity.budget.Budget;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sub_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	// ✅ Each subcategory belongs to one budget
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "budget_id", nullable = false)
	private Budget budget;

	// ✅ Keeps active/inactive status
	@Column(nullable = false)
	private boolean active = true;
}
