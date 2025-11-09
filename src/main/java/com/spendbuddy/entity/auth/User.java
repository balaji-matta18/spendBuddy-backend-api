package com.spendbuddy.entity.auth;

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
	// @GeneratedValue(strategy = GenerationType.UUID)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String username;
	private String email;
	private String password;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference(value = "user-expenses") // Forward part of user-expense relation
	private List<Expense> expenses;
}
