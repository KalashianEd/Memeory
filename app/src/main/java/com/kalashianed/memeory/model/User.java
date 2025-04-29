package com.kalashianed.memeory.model;

/**
 * Класс для хранения информации о пользователе.
 */
public class User {
    private String userId;
    private String username;
    private String email;
    private int bestScore;
    private int bestStreak;

    // Пустой конструктор для Firebase
    public User() {
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.bestScore = 0;
        this.bestStreak = 0;
    }

    // Геттеры и сеттеры
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }
} 