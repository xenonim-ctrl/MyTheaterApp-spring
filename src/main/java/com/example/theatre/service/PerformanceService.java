package com.example.theatre.service;

import com.example.theatre.dto.PerformanceDTO;
import com.example.theatre.dto.PerformanceFilterDTO;
import com.example.theatre.model.Hall;
import com.example.theatre.model.Performance;
import com.example.theatre.model.Play;
import com.example.theatre.repository.HallRepository;
import com.example.theatre.repository.PerformanceRepository;
import com.example.theatre.repository.PlayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с показами
 */
@Service
@Transactional
public class PerformanceService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceService.class);

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private PlayRepository playRepository;

    @Autowired
    private HallRepository hallRepository;

    public Page<Performance> findPerformancesWithFilter(PerformanceFilterDTO filter, Pageable pageable) {
        if (filter == null) {
            filter = new PerformanceFilterDTO();
        }
        Specification<Performance> spec = buildSpecification(filter);
        Sort sort = buildSort(filter.getSortBy(), filter.getSortDirection());
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return performanceRepository.findAll(spec, sortedPageable);
    }

    private Specification<Performance> buildSpecification(PerformanceFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.getPlayId() != null) {
                    predicates.add(cb.equal(root.get("play").get("id"), filter.getPlayId()));
                }

                if (filter.getHallId() != null) {
                    predicates.add(cb.equal(root.get("hall").get("id"), filter.getHallId()));
                }

                if (filter.getStartDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("dateTime"), filter.getStartDate()));
                }

                if (filter.getEndDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("dateTime"), filter.getEndDate()));
                }

                if (filter.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
                }

                if (filter.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "dateTime";
        }
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "asc";
        }
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        String validSortBy = validateSortField(sortBy);
        return Sort.by(direction, validSortBy);
    }

    private String validateSortField(String sortBy) {
        List<String> validFields = List.of("id", "dateTime", "price", "availableSeats");
        return validFields.contains(sortBy) ? sortBy : "dateTime";
    }

    public Performance savePerformance(PerformanceDTO performanceDTO) {
        log.debug("Saving Performance DTO: id={}, playId={}, hallId={}, dateTime={}", 
            performanceDTO.getId(), performanceDTO.getPlayId(), 
            performanceDTO.getHallId(), performanceDTO.getDateTime());
        
        try {
            Performance performance;
            if (performanceDTO.getId() != null) {
                performance = performanceRepository.findById(performanceDTO.getId())
                    .orElseThrow(() -> {
                        log.error("Performance not found with id: {}", performanceDTO.getId());
                        return new RuntimeException("Показ не найден");
                    });
                log.debug("Updating existing performance: {}", performance.getId());
            } else {
                performance = new Performance();
                log.debug("Creating new performance");
            }

            Play play = playRepository.findById(performanceDTO.getPlayId())
                .orElseThrow(() -> {
                    log.error("Play not found with id: {}", performanceDTO.getPlayId());
                    return new RuntimeException("Спектакль не найден");
                });
            log.debug("Found play: {}", play.getTitle());
            
            Hall hall = hallRepository.findById(performanceDTO.getHallId())
                .orElseThrow(() -> {
                    log.error("Hall not found with id: {}", performanceDTO.getHallId());
                    return new RuntimeException("Зал не найден");
                });
            log.debug("Found hall: {}", hall.getName());

            performance.setPlay(play);
            performance.setHall(hall);
            performance.setDateTime(performanceDTO.getDateTime());
            performance.setPrice(performanceDTO.getPrice());
            performance.setAvailableSeats(performanceDTO.getAvailableSeats());

            Performance saved = performanceRepository.save(performance);
            log.debug("Performance saved successfully with id: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Error saving performance", e);
            throw e;
        }
    }

    public Performance findById(Long id) {
        log.debug("Finding Performance by id: {}", id);
        try {
            Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Performance not found with id: {}", id);
                    return new RuntimeException("Показ не найден");
                });
            log.debug("Found performance: {} - {}", performance.getPlay().getTitle(), performance.getDateTime());
            return performance;
        } catch (Exception e) {
            log.error("Error finding performance with id: {}", id, e);
            throw e;
        }
    }

    public void deleteById(Long id) {
        if (!performanceRepository.existsById(id)) {
            throw new RuntimeException("Показ не найден");
        }
        performanceRepository.deleteById(id);
    }

    public List<Performance> findByPlayId(Long playId) {
        log.debug("Finding performances by playId: {}", playId);
        try {
            List<Performance> performances = performanceRepository.findByPlayIdOrderByDateTime(playId);
            log.debug("Found {} performances for play {}", performances.size(), playId);
            return performances;
        } catch (Exception e) {
            log.error("Error finding performances for playId: {}", playId, e);
            throw e;
        }
    }
}

