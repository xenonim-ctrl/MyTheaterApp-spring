package com.example.theatre.controller;

import com.example.theatre.dto.UserAccountDTO;
import com.example.theatre.model.User;
import com.example.theatre.repository.UserRepository;
import com.example.theatre.service.CustomUserDetailsService;
import com.example.theatre.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для управления аккаунтом пользователя
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping
    public String showAccountPage(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Создаем DTO из текущего пользователя
        UserAccountDTO accountDTO = new UserAccountDTO(user.getUsername(), user.getEmail());
        model.addAttribute("accountDTO", accountDTO);
        model.addAttribute("userFullName", user.getFullName());
        model.addAttribute("userRole", user.getRole());

        // Проверяем, является ли пользователь OWNER
        boolean isOwner = user.getRole() == com.example.theatre.model.Role.OWNER;
        model.addAttribute("isOwner", isOwner);

        // Проверяем, является ли пользователь админом
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        // Проверяем, является ли пользователь root админом
        boolean isRootAdmin = Boolean.TRUE.equals(user.getIsRootAdmin());
        model.addAttribute("isRootAdmin", isRootAdmin);

        return "account/account";
    }

    @PostMapping("/update")
    public String updateAccount(@ModelAttribute UserAccountDTO accountDTO, 
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            String oldUsername = authentication.getName();
            User updatedUser = userService.updateProfile(oldUsername, accountDTO);
            
            // Обновляем SecurityContext если username или password изменились
            boolean usernameChanged = !updatedUser.getUsername().equals(oldUsername);
            boolean passwordChanged = accountDTO.getPassword() != null && 
                                     !accountDTO.getPassword().trim().isEmpty();
            
            if (usernameChanged || passwordChanged) {
                // Перезагружаем UserDetails для обновленного пользователя
                UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(updatedUser.getUsername());
                
                // Создаем новую аутентификацию с обновленными данными
                Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails,
                    updatedUserDetails.getPassword(),
                    updatedUserDetails.getAuthorities()
                );
                
                // Обновляем SecurityContext
                SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            }
            
            redirectAttributes.addFlashAttribute("success", "Изменения сохранены");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/account";
    }
}

