package com.example.theatre.repository;

import com.example.theatre.model.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PerformanceRepository extends JpaRepository<Performance, Long>, JpaSpecificationExecutor<Performance> {
    List<Performance> findByPlayId(Long playId);
    List<Performance> findByHallId(Long hallId);
    List<Performance> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT p FROM Performance p WHERE p.play.id = :playId ORDER BY p.dateTime ASC")
    List<Performance> findByPlayIdOrderByDateTime(@Param("playId") Long playId);
}

