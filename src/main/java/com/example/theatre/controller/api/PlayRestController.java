package com.example.theatre.controller.api;

import com.example.theatre.dto.PlayDTO;
import com.example.theatre.dto.PlayFilterDTO;
import com.example.theatre.model.Play;
import com.example.theatre.service.PlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API контроллер для работы со спектаклями
 */
@RestController
@RequestMapping("/api/plays")
public class PlayRestController {

    @Autowired
    private PlayService playService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPlays(
            @ModelAttribute PlayFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Play> playsPage = playService.findPlaysWithFilter(filter, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("plays", playsPage.getContent());
        response.put("currentPage", playsPage.getNumber());
        response.put("totalPages", playsPage.getTotalPages());
        response.put("totalItems", playsPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Play> getPlayById(@PathVariable Long id) {
        Play play = playService.findById(id);
        return ResponseEntity.ok(play);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Play> createPlay(@RequestBody PlayDTO playDTO) {
        Play play = playService.savePlay(playDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(play);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Play> updatePlay(@PathVariable Long id, @RequestBody PlayDTO playDTO) {
        playDTO.setId(id);
        Play play = playService.savePlay(playDTO);
        return ResponseEntity.ok(play);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlay(@PathVariable Long id) {
        playService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

