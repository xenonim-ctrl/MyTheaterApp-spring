package com.example.theatre.service;

import com.example.theatre.dto.PlayDTO;
import com.example.theatre.dto.PlayFilterDTO;
import com.example.theatre.model.Actor;
import com.example.theatre.model.Play;
import com.example.theatre.repository.ActorRepository;
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
import java.util.stream.Collectors;

/**
 * Сервис для работы со спектаклями
 * Реализует бизнес-логику фильтрации, сортировки и пагинации
 */
@Service
@Transactional
public class PlayService {

    private static final Logger log = LoggerFactory.getLogger(PlayService.class);

    @Autowired
    private PlayRepository playRepository;

    @Autowired
    private ActorRepository actorRepository;

    public Page<Play> findPlaysWithFilter(PlayFilterDTO filter, Pageable pageable) {
        Specification<Play> spec = buildSpecification(filter);
        Sort sort = buildSort(filter.getSortBy(), filter.getSortDirection());
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return playRepository.findAll(spec, sortedPageable);
    }

    private Specification<Play> buildSpecification(PlayFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getTitle() != null && !filter.getTitle().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), 
                    "%" + filter.getTitle().toLowerCase() + "%"));
            }

            if (filter.getAuthor() != null && !filter.getAuthor().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("author")), 
                    "%" + filter.getAuthor().toLowerCase() + "%"));
            }

            if (filter.getGenre() != null && !filter.getGenre().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("genre"), filter.getGenre()));
            }

            if (filter.getDirector() != null && !filter.getDirector().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("director")), 
                    "%" + filter.getDirector().toLowerCase() + "%"));
            }

            if (filter.getMinDuration() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("duration"), filter.getMinDuration()));
            }

            if (filter.getMaxDuration() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("duration"), filter.getMaxDuration()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        String validSortBy = validateSortField(sortBy);
        return Sort.by(direction, validSortBy);
    }

    private String validateSortField(String sortBy) {
        List<String> validFields = List.of("id", "title", "author", "genre", "duration", "director");
        return validFields.contains(sortBy) ? sortBy : "id";
    }

    public Play savePlay(PlayDTO playDTO) {
        log.debug("Saving Play DTO: id={}, title={}, actorIds={}", 
            playDTO.getId(), playDTO.getTitle(), playDTO.getActorIds());
        
        try {
            Play play;
            if (playDTO.getId() != null) {
                play = playRepository.findById(playDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Спектакль не найден"));
                log.debug("Updating existing play: {}", play.getId());
            } else {
                play = new Play();
                log.debug("Creating new play");
            }

            play.setTitle(playDTO.getTitle());
            play.setAuthor(playDTO.getAuthor());
            play.setDirector(playDTO.getDirector());
            play.setDescription(playDTO.getDescription());
            play.setDuration(playDTO.getDuration());
            play.setGenre(playDTO.getGenre());

            // Управление актерами
            if (playDTO.getActorIds() != null && !playDTO.getActorIds().isEmpty()) {
                List<Actor> actors = actorRepository.findAllById(playDTO.getActorIds());
                play.setActors(actors);
                log.debug("Set {} actors for play", actors.size());
            } else {
                play.setActors(new ArrayList<>());
                log.debug("No actors selected, clearing actors list");
            }

            Play saved = playRepository.save(play);
            log.debug("Play saved successfully with id: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Error saving play", e);
            throw e;
        }
    }

    public Play findById(Long id) {
        log.debug("Finding Play by id: {}", id);
        try {
            Play play = playRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Play not found with id: {}", id);
                    return new RuntimeException("Спектакль не найден");
                });
            log.debug("Found play: {}", play.getTitle());
            return play;
        } catch (Exception e) {
            log.error("Error finding play with id: {}", id, e);
            throw e;
        }
    }

    public void deleteById(Long id) {
        if (!playRepository.existsById(id)) {
            throw new RuntimeException("Спектакль не найден");
        }
        playRepository.deleteById(id);
    }

    public List<String> getAllGenres() {
        return playRepository.findAll().stream()
            .map(Play::getGenre)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}

