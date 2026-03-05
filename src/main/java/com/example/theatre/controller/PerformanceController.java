package com.example.theatre.controller;

import com.example.theatre.dto.PerformanceDTO;
import com.example.theatre.dto.PerformanceFilterDTO;
import com.example.theatre.model.Performance;
import com.example.theatre.model.User;
import com.example.theatre.repository.HallRepository;
import com.example.theatre.repository.PlayRepository;
import com.example.theatre.repository.UserRepository;
import com.example.theatre.service.PerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Контроллер для работы с показами
 */
@Controller
@RequestMapping("/performances")
public class PerformanceController {

    private static final Logger log = LoggerFactory.getLogger(PerformanceController.class);

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayRepository playRepository;

    @Autowired
    private HallRepository hallRepository;

    @GetMapping
    public String listPerformances(
            @ModelAttribute PerformanceFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Authentication authentication) {
        log.debug("Listing performances - page: {}, size: {}, filter: {}", page, size, filter);
        
        try {
            if (authentication == null || authentication.getName() == null) {
                log.error("Authentication is null or username is null");
                throw new RuntimeException("Пользователь не аутентифицирован");
            }

            User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            log.debug("User: {}, isAdmin: {}", currentUser.getUsername(), isAdmin);
            
            // Ensure filter is not null
            if (filter == null) {
                filter = new PerformanceFilterDTO();
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Performance> performancesPage = performanceService.findPerformancesWithFilter(filter, pageable);
            log.debug("Found {} performances", performancesPage.getTotalElements());
            
            model.addAttribute("performances", performancesPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", performancesPage.getTotalPages());
            model.addAttribute("totalItems", performancesPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("filter", filter);
            model.addAttribute("userFullName", currentUser.getFullName());
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("plays", playRepository.findAll());
            model.addAttribute("halls", hallRepository.findAll());
            
            return "performances/list";
        } catch (Exception e) {
            log.error("Error listing performances", e);
            throw e;
        }
    }

    @GetMapping("/play/{playId}")
    public String listPerformancesByPlay(
            @PathVariable Long playId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, 
            Authentication authentication) {
        log.debug("Listing performances for play: {}", playId);
        
        try {
            if (authentication == null || authentication.getName() == null) {
                log.error("Authentication is null or username is null");
                throw new RuntimeException("Пользователь не аутентифицирован");
            }

            User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            log.debug("User: {}, isAdmin: {}", currentUser.getUsername(), isAdmin);
            
            // Create a filter for the specific play
            PerformanceFilterDTO filter = new PerformanceFilterDTO();
            filter.setPlayId(playId);
            filter.setSortBy("dateTime");
            filter.setSortDirection("asc");
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Performance> performancesPage = performanceService.findPerformancesWithFilter(filter, pageable);
            log.debug("Found {} performances for play {}", performancesPage.getTotalElements(), playId);
            
            model.addAttribute("performances", performancesPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", performancesPage.getTotalPages());
            model.addAttribute("totalItems", performancesPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("filter", filter);
            model.addAttribute("userFullName", currentUser.getFullName());
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("plays", playRepository.findAll());
            model.addAttribute("halls", hallRepository.findAll());
            return "performances/list";
        } catch (Exception e) {
            log.error("Error listing performances for play: {}", playId, e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public String viewPerformance(@PathVariable Long id, Model model, Authentication authentication) {
        log.debug("Opening Performance details: {}", id);
        
        try {
            if (authentication == null || authentication.getName() == null) {
                log.error("Authentication is null or username is null");
                throw new RuntimeException("Пользователь не аутентифицирован");
            }

            Performance performance = performanceService.findById(id);
            log.debug("Found performance: {} - {}", performance.getPlay().getTitle(), performance.getDateTime());

            User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            model.addAttribute("performance", performance);
            model.addAttribute("userFullName", currentUser.getFullName());
            return "performances/view";
        } catch (Exception e) {
            log.error("Error viewing performance with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String showForm(Model model, Authentication authentication) {
        log.debug("Showing new performance form");
        
        try {
            if (authentication != null && authentication.getName() != null) {
                User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
                if (currentUser != null) {
                    model.addAttribute("userFullName", currentUser.getFullName());
                }
            }
            
            model.addAttribute("performance", new PerformanceDTO());
            model.addAttribute("plays", playRepository.findAll());
            model.addAttribute("halls", hallRepository.findAll());
            model.addAttribute("isNew", true);
            return "performances/form";
        } catch (Exception e) {
            log.error("Error showing new performance form", e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String savePerformance(@Valid @ModelAttribute("performance") PerformanceDTO performanceDTO, 
                                  BindingResult bindingResult, 
                                  Model model,
                                  Authentication authentication) {
        log.debug("Saving Performance DTO: {}", performanceDTO);
        
        try {
            if (bindingResult.hasErrors()) {
                log.warn("Validation errors in PerformanceDTO: {}", bindingResult.getAllErrors());
                if (authentication != null && authentication.getName() != null) {
                    User currentUser = userRepository.findByUsername(authentication.getName())
                        .orElse(null);
                    if (currentUser != null) {
                        model.addAttribute("userFullName", currentUser.getFullName());
                    }
                }
                model.addAttribute("plays", playRepository.findAll());
                model.addAttribute("halls", hallRepository.findAll());
                model.addAttribute("isNew", performanceDTO.getId() == null);
                return "performances/form";
            }
            
            performanceService.savePerformance(performanceDTO);
            log.debug("Performance saved successfully with id: {}", performanceDTO.getId());
            return "redirect:/performances";
        } catch (Exception e) {
            log.error("Error saving performance", e);
            throw e;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String editPerformance(@PathVariable Long id, Model model, Authentication authentication) {
        log.debug("Editing Performance with id: {}", id);
        
        try {
            Performance performance = performanceService.findById(id);
            log.debug("Found performance to edit: {} - {}", performance.getPlay().getTitle(), performance.getDateTime());
            
            PerformanceDTO performanceDTO = new PerformanceDTO();
            performanceDTO.setId(performance.getId());
            performanceDTO.setPlayId(performance.getPlay().getId());
            performanceDTO.setHallId(performance.getHall().getId());
            performanceDTO.setDateTime(performance.getDateTime());
            performanceDTO.setPrice(performance.getPrice());
            performanceDTO.setAvailableSeats(performance.getAvailableSeats());
            log.debug("Mapped Performance to DTO");
            
            if (authentication != null && authentication.getName() != null) {
                User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
                if (currentUser != null) {
                    model.addAttribute("userFullName", currentUser.getFullName());
                }
            }
            
            model.addAttribute("performance", performanceDTO);
            model.addAttribute("plays", playRepository.findAll());
            model.addAttribute("halls", hallRepository.findAll());
            model.addAttribute("isNew", false);
            return "performances/form";
        } catch (Exception e) {
            log.error("Error editing performance with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String deletePerformance(@PathVariable Long id) {
        log.debug("Deleting Performance with id: {}", id);
        try {
            performanceService.deleteById(id);
            log.debug("Performance deleted successfully");
            return "redirect:/performances";
        } catch (Exception e) {
            log.error("Error deleting performance with id: {}", id, e);
            throw e;
        }
    }
}

