package com.example.theatre.controller;

import com.example.theatre.model.User;
import com.example.theatre.repository.UserRepository;
import com.example.theatre.service.StatisticsService;
import com.example.theatre.dto.StatisticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для статистики
 */
@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showStatistics(Model model, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        StatisticsDTO stats = statisticsService.calculateStatistics();
        
        model.addAttribute("statistics", stats);
        model.addAttribute("userFullName", currentUser.getFullName());
        model.addAttribute("isAdmin", isAdmin);
        
        return "statistics";
    }
}

