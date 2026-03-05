package com.example.theatre.dto;

import com.example.theatre.model.Role;

/**
 * DTO для отображения пользователей в админ-панели
 */
public class UserAdminDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private Boolean banned;
    private String fullName;
    private Boolean isRootAdmin;

    public UserAdminDTO() {
    }

    public UserAdminDTO(Long id, String username, String email, Role role, Boolean banned, String fullName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.banned = banned;
        this.fullName = fullName;
    }

    public UserAdminDTO(Long id, String username, String email, Role role, Boolean banned, String fullName, Boolean isRootAdmin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.banned = banned;
        this.fullName = fullName;
        this.isRootAdmin = isRootAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getBanned() {
        return banned;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getIsRootAdmin() {
        return isRootAdmin;
    }

    public void setIsRootAdmin(Boolean isRootAdmin) {
        this.isRootAdmin = isRootAdmin;
    }
}

