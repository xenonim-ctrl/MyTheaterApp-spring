package com.example.theatre.service;

import com.example.theatre.dto.UserAccountDTO;
import com.example.theatre.dto.UserAdminDTO;
import com.example.theatre.dto.UserRegistrationDTO;
import com.example.theatre.model.Role;
import com.example.theatre.model.User;
import com.example.theatre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        // Валидация пароля
        String password = registrationDTO.getPassword();
        if (password == null || password.trim().length() < 6) {
            throw new RuntimeException("Пароль должен содержать минимум 6 символов");
        }

        // Валидация телефона (русский формат)
        String phone = registrationDTO.getPhone().trim();
        if (!phone.matches("^(\\+7|8)[0-9]{10}$")) {
            throw new RuntimeException("Телефон должен начинаться с +7 или 8 и содержать 11 цифр");
        }

        // Нормализация телефона (приводим к формату +7XXXXXXXXXX)
        if (phone.startsWith("8")) {
            phone = "+7" + phone.substring(1);
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(Role.USER); // Всегда USER для новых регистраций (OWNER нельзя создать через регистрацию)
        user.setFullName(registrationDTO.getFullName());
        user.setPhone(phone);
        user.setEmail(registrationDTO.getEmail().trim().toLowerCase());

        user.setBanned(false); // Новые пользователи не забанены
        user.setIsRootAdmin(false); // Новые пользователи не являются root администраторами
        
        // Проверка: нельзя создать OWNER через регистрацию
        if (user.getRole() == Role.OWNER) {
            throw new IllegalStateException("Невозможно создать пользователя с ролью OWNER через регистрацию");
        }
        
        return userRepository.save(user);
    }

    /**
     * Проверяет, является ли пользователь OWNER по username
     */
    public boolean isOwner(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRole() == Role.OWNER)
                .orElse(false);
    }

    /**
     * Проверяет, является ли пользователь OWNER по ID
     */
    public boolean isOwner(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == Role.OWNER)
                .orElse(false);
    }

    /**
     * Проверяет, является ли пользователь root админом по username
     */
    public boolean isRootAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> Boolean.TRUE.equals(user.getIsRootAdmin()))
                .orElse(false);
    }

    /**
     * Проверяет, является ли пользователь root админом по ID
     */
    public boolean isRootAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getIsRootAdmin()))
                .orElse(false);
    }

    /**
     * Проверяет, существует ли уже OWNER в системе
     */
    @Transactional(readOnly = true)
    public boolean ownerExists() {
        return userRepository.countByRole(Role.OWNER) > 0;
    }

    /**
     * Получить всех пользователей для админ-панели
     */
    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllUsersForAdmin() {
        return userRepository.findAll().stream()
                .map(user -> new UserAdminDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getBanned(),
                        user.getFullName(),
                        user.getIsRootAdmin()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Изменить роль пользователя
     * @param userId ID пользователя
     * @param newRole Новая роль (USER или ADMIN, OWNER запрещен)
     * @param currentUsername Имя текущего пользователя (для проверок)
     */
    @Transactional
    public void changeUserRole(Long userId, Role newRole, String currentUsername) {
        // Блокируем присвоение роли OWNER через этот метод
        if (newRole == Role.OWNER) {
            throw new IllegalStateException("Невозможно присвоить роль OWNER. Роль OWNER назначается только вручную в базе данных.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Защита OWNER - никто не может изменить роль OWNER
        if (user.getRole() == Role.OWNER) {
            throw new RuntimeException("Нельзя изменить роль владельца системы (OWNER)");
        }

        // Защита root админа (только если не OWNER выполняет операцию)
        if (Boolean.TRUE.equals(user.getIsRootAdmin()) && !isOwner(currentUsername)) {
            throw new RuntimeException("Нельзя изменить роль root администратора");
        }

        // Устанавливаем новую роль
        user.setRole(newRole);

        // Автоматически устанавливаем isRootAdmin в зависимости от роли
        if (newRole == Role.ADMIN) {
            user.setIsRootAdmin(true);
        } else {
            user.setIsRootAdmin(false);
        }

        userRepository.save(user);
    }

    /**
     * Забанить пользователя
     */
    @Transactional
    public void banUser(Long userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Защита OWNER - никто не может забанить OWNER
        if (user.getRole() == Role.OWNER) {
            throw new RuntimeException("Нельзя забанить владельца системы (OWNER)");
        }

        // Защита root админа (только если не OWNER выполняет операцию)
        if (Boolean.TRUE.equals(user.getIsRootAdmin()) && !isOwner(currentUsername)) {
            throw new RuntimeException("Нельзя забанить root администратора");
        }

        user.setBanned(true);
        userRepository.save(user);
    }

    /**
     * Разбанить пользователя
     */
    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setBanned(false);
        userRepository.save(user);
    }

    /**
     * Удалить пользователя
     */
    @Transactional
    public void deleteUser(Long userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Защита OWNER - никто не может удалить OWNER
        if (user.getRole() == Role.OWNER) {
            throw new RuntimeException("Нельзя удалить владельца системы (OWNER)");
        }

        // Защита root админа (только если не OWNER выполняет операцию)
        if (Boolean.TRUE.equals(user.getIsRootAdmin()) && !isOwner(currentUsername)) {
            throw new RuntimeException("Нельзя удалить root администратора");
        }

        // Защита от удаления самого себя
        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Нельзя удалить самого себя");
        }

        userRepository.delete(user);
    }

    /**
     * Получить пользователя по ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Обновить профиль пользователя (username, email, пароль опционально)
     * Роль, banned статус и isRootAdmin не могут быть изменены через этот метод
     */
    @Transactional
    public User updateProfile(String username, UserAccountDTO accountDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Роль OWNER не может быть изменена (но пользователь может редактировать свой профиль)
        // Этот метод не изменяет роль, так что это безопасно

        // Проверка на уникальность username, если он изменен
        if (!user.getUsername().equals(accountDTO.getUsername())) {
            if (userRepository.existsByUsername(accountDTO.getUsername())) {
                throw new RuntimeException("Пользователь с таким именем уже существует");
            }
            user.setUsername(accountDTO.getUsername());
        }

        // Обновление email
        if (accountDTO.getEmail() != null && !accountDTO.getEmail().trim().isEmpty()) {
            user.setEmail(accountDTO.getEmail().trim().toLowerCase());
        }

        // Обновление пароля, если он указан
        if (accountDTO.getPassword() != null && !accountDTO.getPassword().trim().isEmpty()) {
            if (!accountDTO.getPassword().equals(accountDTO.getConfirmPassword())) {
                throw new RuntimeException("Пароли не совпадают");
            }
            if (accountDTO.getPassword().length() < 6) {
                throw new RuntimeException("Пароль должен содержать минимум 6 символов");
            }
            user.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        }

        return userRepository.save(user);
    }
}

