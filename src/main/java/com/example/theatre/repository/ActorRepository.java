package com.example.theatre.repository;

import com.example.theatre.model.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    List<Actor> findByFullNameContainingIgnoreCase(String name);
}

