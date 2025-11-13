package com.spendbuddy.entity.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.spendbuddy.entity.expensetracker.Expense;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

	public User(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;
	private String email;

	@JsonIgnore
	private String password;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference(value = "user-expenses")
	private List<Expense> expenses;

	// ✅ New field: allows each user to set their custom month start day (1–28)
	@Column(name = "month_start_day")
	private Integer monthStartDay = 1; // Default to 1st of the month
}
