package com.example.theatre.controller.api;

import com.example.theatre.dto.StatisticsDTO;
import com.example.theatre.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API контроллер для статистики
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsRestController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<StatisticsDTO> getStatistics() {
        StatisticsDTO stats = statisticsService.calculateStatistics();
        return ResponseEntity.ok(stats);
    }
}
