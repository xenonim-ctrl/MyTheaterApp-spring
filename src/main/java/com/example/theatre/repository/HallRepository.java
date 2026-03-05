package com.example.theatre.repository;

import com.example.theatre.model.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {
    List<Hall> findByNameContainingIgnoreCase(String name);
}

