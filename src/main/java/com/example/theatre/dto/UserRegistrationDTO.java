package com.example.theatre.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserRegistrationDTO {
    
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;
    
    @NotBlank(message = "Пароль обязателен")
    private String password;
    
    @NotBlank(message = "Полное имя обязательно")
    private String fullName;
    
    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^(\\+7|8)[0-9]{10}$", message = "Телефон должен начинаться с +7 или 8 и содержать 11 цифр")
    private String phone;
    
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    // Геттеры и сеттеры

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

