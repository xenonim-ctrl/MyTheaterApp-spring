package com.example.theatre.service;

import com.example.theatre.dto.StatisticsDTO;
import com.example.theatre.model.Performance;
import com.example.theatre.model.Play;
import com.example.theatre.repository.PerformanceRepository;
import com.example.theatre.repository.PlayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис для расчета статистики театра
 */
@Service
@Transactional(readOnly = true)
public class StatisticsService {

    @Autowired
    private PlayRepository playRepository;

    @Autowired
    private PerformanceRepository performanceRepository;


    public StatisticsDTO calculateStatistics() {
        StatisticsDTO stats = new StatisticsDTO();
        
        List<Play> plays = playRepository.findAll();
        List<Performance> performances = performanceRepository.findAll();

        // Общее количество спектаклей
        stats.setTotalPlays((long) plays.size());

        // Общее количество показов
        stats.setTotalPerformances((long) performances.size());

        // Статистика по ценам
        List<Double> prices = performances.stream()
            .map(Performance::getPrice)
            .toList();
        
        if (!prices.isEmpty()) {
            stats.setAverageTicketPrice(prices.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0));
            
            stats.setMinTicketPrice(prices.stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0));
            
            stats.setMaxTicketPrice(prices.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0));
        } else {
            stats.setAverageTicketPrice(0.0);
            stats.setMinTicketPrice(0.0);
            stats.setMaxTicketPrice(0.0);
        }

        // Группировка спектаклей по жанрам
        Map<String, Long> playsByGenre = plays.stream()
            .collect(Collectors.groupingBy(
                Play::getGenre,
                Collectors.counting()
            ));
        stats.setPlaysByGenre(playsByGenre);

        // Группировка показов по залам
        Map<String, Long> performancesByHall = performances.stream()
            .collect(Collectors.groupingBy(
                p -> p.getHall().getName(),
                Collectors.counting()
            ));
        stats.setPerformancesByHall(performancesByHall);






        return stats;
    }
}
