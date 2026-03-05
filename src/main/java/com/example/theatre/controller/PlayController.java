package com.example.theatre.controller;

import com.example.theatre.dto.PlayDTO;
import com.example.theatre.dto.PlayFilterDTO;
import com.example.theatre.model.Actor;
import com.example.theatre.model.Play;
import com.example.theatre.model.User;
import com.example.theatre.repository.ActorRepository;
import com.example.theatre.repository.UserRepository;
import com.example.theatre.service.PlayService;
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
 * Контроллер для работы со спектаклями
 */
@Controller
@RequestMapping("/plays")
public class PlayController {

    private static final Logger log = LoggerFactory.getLogger(PlayController.class);

    @Autowired
    private PlayService playService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActorRepository actorRepository;

    @GetMapping
    public String listPlays(
            @ModelAttribute PlayFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Authentication authentication) {
        log.debug("Listing plays - page: {}, size: {}, filter: {}", page, size, filter);
        
        try {
            if (authentication == null || authentication.getName() == null) {
                log.error("Authentication is null or username is null");
                throw new RuntimeException("Пользователь не аутентифицирован");
            }

            User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            log.debug("User: {}, isAdmin: {}", currentUser.getUsername(), isAdmin);

            Pageable pageable = PageRequest.of(page, size);
            Page<Play> playsPage = playService.findPlaysWithFilter(filter, pageable);
            log.debug("Found {} plays", playsPage.getTotalElements());

            model.addAttribute("plays", playsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", playsPage.getTotalPages());
            model.addAttribute("totalItems", playsPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("filter", filter);
            model.addAttribute("userFullName", currentUser.getFullName());
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("genres", playService.getAllGenres());

            return "plays/list";
        } catch (Exception e) {
            log.error("Error listing plays", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public String viewPlay(@PathVariable Long id, Model model, Authentication authentication) {
        log.debug("Opening Play details: {}", id);
        
        try {
            if (authentication == null || authentication.getName() == null) {
                log.error("Authentication is null or username is null");
                throw new RuntimeException("Пользователь не аутентифицирован");
            }

            Play play = playService.findById(id);
            log.debug("Found play: {}", play.getTitle());

            User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            model.addAttribute("play", play);
            model.addAttribute("userFullName", currentUser.getFullName());
            return "plays/view";
        } catch (Exception e) {
            log.error("Error viewing play with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String showForm(Model model, Authentication authentication) {
        log.debug("Showing new play form");
        
        try {
            if (authentication != null && authentication.getName() != null) {
                User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
                if (currentUser != null) {
                    model.addAttribute("userFullName", currentUser.getFullName());
                }
            }
            
            model.addAttribute("play", new PlayDTO());
            model.addAttribute("actors", actorRepository.findAll());
            model.addAttribute("isNew", true);
            return "plays/form";
        } catch (Exception e) {
            log.error("Error showing new play form", e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String savePlay(@Valid @ModelAttribute("play") PlayDTO playDTO, 
                          BindingResult bindingResult, 
                          Model model,
                          Authentication authentication) {
        log.debug("Saving Play DTO: {}", playDTO);
        
        try {
            if (bindingResult.hasErrors()) {
                log.warn("Validation errors in PlayDTO: {}", bindingResult.getAllErrors());
                if (authentication != null && authentication.getName() != null) {
                    User currentUser = userRepository.findByUsername(authentication.getName())
                        .orElse(null);
                    if (currentUser != null) {
                        model.addAttribute("userFullName", currentUser.getFullName());
                    }
                }
                model.addAttribute("actors", actorRepository.findAll());
                model.addAttribute("isNew", playDTO.getId() == null);
                return "plays/form";
            }
            
            playService.savePlay(playDTO);
            log.debug("Play saved successfully with id: {}", playDTO.getId());
            return "redirect:/plays";
        } catch (Exception e) {
            log.error("Error saving play", e);
            throw e;
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String editPlay(@PathVariable Long id, Model model, Authentication authentication) {
        log.debug("Editing Play with id: {}", id);
        
        try {
            Play play = playService.findById(id);
            log.debug("Found play to edit: {}", play.getTitle());

            PlayDTO playDTO = new PlayDTO();
            playDTO.setId(play.getId());
            playDTO.setTitle(play.getTitle());
            playDTO.setAuthor(play.getAuthor());
            playDTO.setDirector(play.getDirector());
            playDTO.setDescription(play.getDescription());
            playDTO.setDuration(play.getDuration());
            playDTO.setGenre(play.getGenre());
            playDTO.setActorIds(play.getActors().stream().map(Actor::getId).toList());
            log.debug("Mapped Play to DTO with {} actors", playDTO.getActorIds() != null ? playDTO.getActorIds().size() : 0);

            if (authentication != null && authentication.getName() != null) {
                User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
                if (currentUser != null) {
                    model.addAttribute("userFullName", currentUser.getFullName());
                }
            }

            model.addAttribute("play", playDTO);
            model.addAttribute("actors", actorRepository.findAll());
            model.addAttribute("isNew", false);
            return "plays/form";
        } catch (Exception e) {
            log.error("Error editing play with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String deletePlay(@PathVariable Long id) {
        log.debug("Deleting Play with id: {}", id);
        try {
            playService.deleteById(id);
            log.debug("Play deleted successfully");
            return "redirect:/plays";
        } catch (Exception e) {
            log.error("Error deleting play with id: {}", id, e);
            throw e;
        }
    }
}

