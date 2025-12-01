package com.vaultsys;

/**
 * User data model - represents a logged-in user
 * Simple POJO with immutable fields (no setters)
 * Demonstrates encapsulation - private fields with public getters
 */
public class User {
    private int id; // Database primary key
    private String username; // Unique username
    private String role; // CUSTOMER or ADMIN

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
