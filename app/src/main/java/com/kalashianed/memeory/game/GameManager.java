package com.kalashianed.memeory.game;

import android.content.Context;
import android.content.SharedPreferences;

import com.kalashianed.memeory.data.MemeData;
import com.kalashianed.memeory.model.Meme;

import java.util.List;

/**
 * Класс, управляющий игровым процессом угадывания мемов
 */
public class GameManager {
    private List<Meme> memeList;
    private int currentMemeIndex;
    private int score;
    private int totalAttempts;
    private int currentStreak; // Текущая серия правильных ответов
    private int bestStreak; // Лучшая серия правильных ответов
    private int currentLevel; // Текущий уровень игрока
    private MemeData.Difficulty difficulty;
    private Context context;
    
    // Константы для SharedPreferences
    public static final String PREF_NAME = "MemeoryPrefs";
    public static final String KEY_BEST_STREAK = "best_streak";
    public static final String KEY_LEVEL = "player_level";
    public static final String KEY_BEST_SCORE = "best_score_percentage";
    
    /**
     * Создает новый игровой менеджер
     * @param context контекст приложения
     * @param difficulty уровень сложности
     */
    public GameManager(Context context, MemeData.Difficulty difficulty) {
        this.context = context;
        this.difficulty = difficulty;
        loadUserStats();
        resetGame();
    }
    
    /**
     * Загружает статистику пользователя
     */
    private void loadUserStats() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        bestStreak = prefs.getInt(KEY_BEST_STREAK, 0);
        currentLevel = prefs.getInt(KEY_LEVEL, 1);
    }
    
    /**
     * Сохраняет статистику пользователя
     */
    private void saveUserStats() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_BEST_STREAK, bestStreak);
        editor.putInt(KEY_LEVEL, currentLevel);
        
        // Сохраняем также лучший процент правильных ответов
        int currentPercentage = getScorePercentage();
        int bestScore = prefs.getInt(KEY_BEST_SCORE, 0);
        
        if (currentPercentage > bestScore) {
            editor.putInt(KEY_BEST_SCORE, currentPercentage);
        }
        
        editor.apply();
    }
    
    /**
     * Сбрасывает игру до начального состояния
     */
    public void resetGame() {
        memeList = MemeData.getShuffledMemeList(context, difficulty);
        currentMemeIndex = 0;
        score = 0;
        totalAttempts = 0;
        currentStreak = 0;
    }
    
    /**
     * Изменяет уровень сложности и сбрасывает игру
     * @param difficulty новый уровень сложности
     */
    public void setDifficulty(MemeData.Difficulty difficulty) {
        this.difficulty = difficulty;
        resetGame();
    }
    
    /**
     * @return текущий уровень сложности
     */
    public MemeData.Difficulty getDifficulty() {
        return difficulty;
    }
    
    /**
     * Проверяет правильность выбранного ответа
     * @param selectedOption выбранный вариант ответа
     * @return true, если ответ правильный
     */
    public boolean checkAnswer(String selectedOption) {
        totalAttempts++;
        boolean isCorrect = getCurrentMeme().getCorrectName().equals(selectedOption);
        if (isCorrect) {
            score++;
            currentStreak++;
            
            // Проверка на рекорд серии
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
                
                // Повышаем уровень каждые 5 единиц серии
                if (bestStreak % 5 == 0) {
                    currentLevel++;
                }
                
                // Сохраняем новые достижения
                saveUserStats();
            }
        } else {
            // Сбрасываем текущую серию при неправильном ответе
            currentStreak = 0;
        }
        return isCorrect;
    }
    
    /**
     * Переходит к следующему мему
     * @return true, если есть следующий мем, false если достигнут конец списка
     */
    public boolean nextMeme() {
        currentMemeIndex++;
        return currentMemeIndex < memeList.size();
    }
    
    /**
     * @return текущий мем
     */
    public Meme getCurrentMeme() {
        return memeList.get(currentMemeIndex);
    }
    
    /**
     * @return текущий счет
     */
    public int getScore() {
        return score;
    }
    
    /**
     * @return общее количество попыток
     */
    public int getTotalAttempts() {
        return totalAttempts;
    }
    
    /**
     * @return процент правильных ответов
     */
    public int getScorePercentage() {
        if (totalAttempts == 0) return 0;
        return (score * 100) / totalAttempts;
    }
    
    /**
     * @return текущий прогресс игры (номер текущего мема / общее количество)
     */
    public String getProgress() {
        return (currentMemeIndex + 1) + "/" + memeList.size();
    }
    
    /**
     * @return true, если есть еще мемы для отображения
     */
    public boolean hasMoreMemes() {
        return currentMemeIndex < memeList.size();
    }
    
    /**
     * @return текущая серия правильных ответов
     */
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    /**
     * @return лучшая серия правильных ответов
     */
    public int getBestStreak() {
        return bestStreak;
    }
    
    /**
     * @return текущий уровень игрока
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
} 