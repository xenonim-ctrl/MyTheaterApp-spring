package com.example.theatre.repository;

import com.example.theatre.model.Play;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface PlayRepository extends JpaRepository<Play, Long>, JpaSpecificationExecutor<Play> {
    List<Play> findByGenre(String genre);
    List<Play> findByTitleContainingIgnoreCase(String title);
}

