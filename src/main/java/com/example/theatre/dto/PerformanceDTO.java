package com.example.theatre.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

/**
 * DTO для операций с показами
 */
public class PerformanceDTO {
    private Long id;

    @NotNull(message = "ID спектакля обязателен")
    private Long playId;

    @NotNull(message = "ID зала обязателен")
    private Long hallId;

    @NotNull(message = "Дата и время обязательны")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateTime;

    @NotNull(message = "Цена обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private Double price;

    @NotNull(message = "Количество мест обязательно")
    @Min(value = 1, message = "Количество мест должно быть больше 0")
    private Integer availableSeats;

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayId() {
        return playId;
    }

    public void setPlayId(Long playId) {
        this.playId = playId;
    }

    public Long getHallId() {
        return hallId;
    }

    public void setHallId(Long hallId) {
        this.hallId = hallId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
}

