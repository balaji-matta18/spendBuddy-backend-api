package com.spendbuddy.response.dto;

import java.util.Date;

public class CategoryResponse {
	private Long id;
	private String name;
	private Long userId;
	private Date createdAt;
	private Date updatedAt;

	public CategoryResponse(Long id, String name, Long userId, Date createdAt, Date updatedAt) {
		this.id = id;
		this.name = name;
		this.userId = userId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	// getters and setters
	public Long getId() { return id; }
	public String getName() { return name; }
	public Long getUserId() { return userId; }
	public Date getCreatedAt() { return createdAt; }
	public Date getUpdatedAt() { return updatedAt; }

	public void setId(Long id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setUserId(Long userId) { this.userId = userId; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
