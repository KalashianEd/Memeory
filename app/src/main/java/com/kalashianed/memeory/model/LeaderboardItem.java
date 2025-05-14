package com.kalashianed.memeory.model;

/**
 * Класс для хранения данных элемента таблицы лидеров.
 */
public class LeaderboardItem {
    private String userId;
    private String username;
    private int score;
    private int rank;

    // Пустой конструктор для Firebase
    public LeaderboardItem() {
    }

    public LeaderboardItem(String userId, String username, int score, int rank) {
        this.userId = userId;
        this.username = username;
        this.score = score;
        this.rank = rank;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
} 