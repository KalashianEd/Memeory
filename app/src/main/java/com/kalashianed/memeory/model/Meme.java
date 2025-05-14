package com.kalashianed.memeory.model;

/**
 * Класс, представляющий мем в приложении
 */
public class Meme {
    private int imageResId;
    private String correctName;
    private String[] options;
    private MemeCategory category;

    /**
     * Конструктор с ресурсом изображения и категорией
     */
    public Meme(int imageResId, String correctName, String[] options, MemeCategory category) {
        this.imageResId = imageResId;
        this.correctName = correctName;
        this.options = options;
        this.category = category;
    }

    /**
     * Конструктор с автоматическим определением категории
     */
    public Meme(int imageResId, String correctName, String[] options) {
        this.imageResId = imageResId;
        this.correctName = correctName;
        this.options = options;
        this.category = determineMemeCategory(correctName);
    }
    
    /**
     * Определяет категорию мема по его названию
     */
    public static MemeCategory determineMemeCategory(String memeName) {
        // Автоматически определяем категорию по имени
        String lowerName = memeName.toLowerCase();
        if (lowerName.contains("толик") || 
            lowerName.contains("подъезд") ||
            lowerName.contains("что если") ||
            lowerName.contains("ну давай") ||
            lowerName.contains("поворот") ||
            lowerName.contains("ждун") ||
            lowerName.contains("происходит") ||
            lowerName.contains("в этом весь") ||
            lowerName.contains("тапки") ||
            lowerName.contains("гэтсби") ||
            lowerName.contains("фрай")) {
            return MemeCategory.RUSSIAN;
        } else if (lowerName.contains("drake") ||
                  lowerName.contains("distracted") ||
                  lowerName.contains("boyfriend") ||
                  lowerName.contains("harold") ||
                  lowerName.contains("this is fine") ||
                  lowerName.contains("jordan") ||
                  lowerName.contains("success") ||
                  lowerName.contains("doge") ||
                  lowerName.contains("cat") ||
                  lowerName.contains("brian") ||
                  lowerName.contains("brain") ||
                  lowerName.contains("roll safe")) {
            return MemeCategory.ENGLISH;
        } else {
            return MemeCategory.OTHER;
        }
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getCorrectName() {
        return correctName;
    }

    public String[] getOptions() {
        return options;
    }
    
    public MemeCategory getCategory() {
        return category;
    }
} 