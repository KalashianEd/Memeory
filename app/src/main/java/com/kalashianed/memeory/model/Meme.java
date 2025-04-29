package com.kalashianed.memeory.model;

/**
 * Класс, представляющий мем в приложении
 */
public class Meme {
    private int imageResId;
    private String imageUrl;
    private String correctName;
    private String[] options;

    /**
     * Конструктор с ресурсом изображения (для placeholder)
     */
    public Meme(int imageResId, String correctName, String[] options) {
        this.imageResId = imageResId;
        this.correctName = correctName;
        this.options = options;
        this.imageUrl = null;
    }
    
    /**
     * Конструктор с URL изображения
     */
    public Meme(int imageResId, String imageUrl, String correctName, String[] options) {
        this.imageResId = imageResId;
        this.imageUrl = imageUrl;
        this.correctName = correctName;
        this.options = options;
    }

    public int getImageResId() {
        return imageResId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public boolean hasImageUrl() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public String getCorrectName() {
        return correctName;
    }

    public String[] getOptions() {
        return options;
    }
} 