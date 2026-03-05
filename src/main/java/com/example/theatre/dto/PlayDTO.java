package com.example.theatre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * DTO для операций со спектаклями
 */
public class PlayDTO {
    private Long id;

    @NotBlank(message = "Название спектакля обязательно")
    private String title;

    @NotBlank(message = "Автор обязателен")
    private String author;

    private String director;
    private String description;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 1, message = "Длительность должна быть больше 0")
    private Integer duration;

    @NotBlank(message = "Жанр обязателен")
    private String genre;

    private List<Long> actorIds;

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public List<Long> getActorIds() {
        return actorIds;
    }

    public void setActorIds(List<Long> actorIds) {
        this.actorIds = actorIds;
    }
}

