package com.example.theatre.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Модель спектакля (родительская сущность)
 * Имеет связь один-ко-многим с Performance (многоуровневая структура)
 */
@Entity
@Table(name = "plays")
public class Play {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "author", nullable = false, length = 200)
    private String author;

    @Column(name = "director", length = 200)
    private String director;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "duration", nullable = false)
    private Integer duration; // в минутах

    @Column(name = "genre", nullable = false, length = 100)
    private String genre;

    @OneToMany(mappedBy = "play", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Performance> performances = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "play_actors",
        joinColumns = @JoinColumn(name = "play_id"),
        inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private List<Actor> actors = new ArrayList<>();

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

    public List<Performance> getPerformances() {
        return performances;
    }

    public void setPerformances(List<Performance> performances) {
        this.performances = performances;
    }

    public void addPerformance(Performance performance) {
        performances.add(performance);
        performance.setPlay(this);
    }

    public void removePerformance(Performance performance) {
        performances.remove(performance);
        performance.setPlay(null);
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }
}

