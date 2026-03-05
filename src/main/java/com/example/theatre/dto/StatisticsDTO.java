package com.example.theatre.dto;

import java.util.Map;

/**
 * DTO для статистики театра
 */
public class StatisticsDTO {
    private Long totalPlays;
    private Long totalPerformances;
    private Long totalTickets;
    private Double averageTicketPrice;
    private Double minTicketPrice;
    private Double maxTicketPrice;
    private Map<String, Long> playsByGenre;
    private Map<String, Long> performancesByHall;
    private Long totalRevenue;
    private Double averageSeatsOccupancy;

    // Геттеры и сеттеры

    public Long getTotalPlays() {
        return totalPlays;
    }

    public void setTotalPlays(Long totalPlays) {
        this.totalPlays = totalPlays;
    }

    public Long getTotalPerformances() {
        return totalPerformances;
    }

    public void setTotalPerformances(Long totalPerformances) {
        this.totalPerformances = totalPerformances;
    }

    public Long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Double getAverageTicketPrice() {
        return averageTicketPrice;
    }

    public void setAverageTicketPrice(Double averageTicketPrice) {
        this.averageTicketPrice = averageTicketPrice;
    }

    public Double getMinTicketPrice() {
        return minTicketPrice;
    }

    public void setMinTicketPrice(Double minTicketPrice) {
        this.minTicketPrice = minTicketPrice;
    }

    public Double getMaxTicketPrice() {
        return maxTicketPrice;
    }

    public void setMaxTicketPrice(Double maxTicketPrice) {
        this.maxTicketPrice = maxTicketPrice;
    }

    public Map<String, Long> getPlaysByGenre() {
        return playsByGenre;
    }

    public void setPlaysByGenre(Map<String, Long> playsByGenre) {
        this.playsByGenre = playsByGenre;
    }

    public Map<String, Long> getPerformancesByHall() {
        return performancesByHall;
    }

    public void setPerformancesByHall(Map<String, Long> performancesByHall) {
        this.performancesByHall = performancesByHall;
    }

    public Long getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getAverageSeatsOccupancy() {
        return averageSeatsOccupancy;
    }

    public void setAverageSeatsOccupancy(Double averageSeatsOccupancy) {
        this.averageSeatsOccupancy = averageSeatsOccupancy;
    }
}
