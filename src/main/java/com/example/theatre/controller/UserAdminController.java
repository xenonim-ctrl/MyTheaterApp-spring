package com.example.theatre.controller;

import com.example.theatre.dto.UserAdminDTO;
import com.example.theatre.model.User;
import com.example.theatre.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Контроллер для управления пользователями (только для root админа)
 */
@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    @Autowired
    private UserService userService;

    /**
     * Проверяет, является ли текущий пользователь OWNER
     */
    private boolean isOwner(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }
        return userService.isOwner(authentication.getName());
    }

    /**
     * Проверяет, является ли текущий пользователь root админом
     */
    private boolean isRootAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }
        String username = authentication.getName();
        return userService.isRootAdmin(username);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String showUsersPage(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        // Проверка, что пользователь аутентифицирован
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется аутентификация.");
            return "redirect:/login";
        }

        // Проверка, что это OWNER или root админ
        boolean isOwner = isOwner(authentication);
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isRootAdmin = isRootAdmin(authentication);

        if (!isOwner && (!isAdmin || !isRootAdmin)) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен. Только OWNER или root администратор могут управлять пользователями.");
            return "redirect:/plays";
        }

        try {
            List<UserAdminDTO> users = userService.getAllUsersForAdmin();
            model.addAttribute("users", users);
            model.addAttribute("currentUsername", authentication.getName());
            model.addAttribute("currentUserIsOwner", isOwner);
            return "admin/admin-users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке пользователей: " + e.getMessage());
            return "redirect:/plays";
        }
    }

    @PostMapping("/role")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String changeRole(@RequestParam Long userId, 
                            @RequestParam String newRole,
                            Authentication authentication, 
                            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется аутентификация.");
            return "redirect:/login";
        }

        boolean isOwner = isOwner(authentication);
        boolean isRootAdmin = isRootAdmin(authentication);

        if (!isOwner && !isRootAdmin) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен. Только OWNER или root администратор могут управлять пользователями.");
            return "redirect:/plays";
        }

        // Проверка: если редактируется OWNER, только сам OWNER может это сделать (но изменение роли OWNER запрещено в сервисе)
        User targetUser = userService.getUserById(userId);
        if (targetUser.getRole() == com.example.theatre.model.Role.OWNER && !isOwner) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен. Только OWNER может управлять учетной записью OWNER.");
            return "redirect:/admin/users";
        }

        try {
            // Валидация роли - блокируем OWNER
            com.example.theatre.model.Role role;
            try {
                role = com.example.theatre.model.Role.valueOf(newRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "Недопустимая роль: " + newRole);
                return "redirect:/admin/users";
            }

            // Дополнительная проверка: OWNER не может быть присвоен через UI
            if (role == com.example.theatre.model.Role.OWNER) {
                redirectAttributes.addFlashAttribute("error", "Невозможно присвоить роль OWNER. Роль OWNER назначается только вручную в базе данных.");
                return "redirect:/admin/users";
            }

            userService.changeUserRole(userId, role, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Роль пользователя изменена.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/ban")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String banUser(@RequestParam Long userId, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется аутентификация.");
            return "redirect:/login";
        }

        boolean isOwner = isOwner(authentication);
        boolean isRootAdmin = isRootAdmin(authentication);

        if (!isOwner && !isRootAdmin) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен. Только OWNER или root администратор могут управлять пользователями.");
            return "redirect:/plays";
        }

        // Проверка: нельзя забанить OWNER
        com.example.theatre.model.User targetUser = userService.getUserById(userId);
        if (targetUser.getRole() == com.example.theatre.model.Role.OWNER) {
            redirectAttributes.addFlashAttribute("error", "Нельзя забанить владельца системы (OWNER).");
            return "redirect:/admin/users";
        }

        try {
            userService.banUser(userId, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Пользователь забанен.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/unban")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String unbanUser(@RequestParam Long userId, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется аутентификация.");
            return "redirect:/login";
        }

        boolean isOwner = isOwner(authentication);
        boolean isRootAdmin = isRootAdmin(authentication);

        if (!isOwner && !isRootAdmin) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен. Только OWNER или root администратор могут управлять пользователями.");
            return "redirect:/plays";
        }

        try {
            userService.unbanUser(userId);
            redirectAttributes.addFlashAttribute("success", "Пользователь разбанен.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String deleteUser(@RequestParam Long userId, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется аутентификация.");
            return "redirect:/login";
        }

        boolean isOwner = isOwner(authentication);
        boolean isRootAdmin = isRootAdmin(authentication);

        if (!isOwner && !isRootAdmin) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен. Только OWNER или root администратор могут управлять пользователями.");
            return "redirect:/plays";
        }

        // Проверка: нельзя удалить OWNER
        com.example.theatre.model.User targetUser = userService.getUserById(userId);
        if (targetUser.getRole() == com.example.theatre.model.Role.OWNER) {
            redirectAttributes.addFlashAttribute("error", "Нельзя удалить владельца системы (OWNER).");
            return "redirect:/admin/users";
        }

        try {
            userService.deleteUser(userId, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Пользователь удален.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }
}

