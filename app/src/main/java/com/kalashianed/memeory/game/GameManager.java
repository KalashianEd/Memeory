package com.kalashianed.memeory.game;

import android.content.Context;
import android.content.SharedPreferences;

import com.kalashianed.memeory.data.MemeData;
import com.kalashianed.memeory.model.Meme;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

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
    private MemeData.CategoryType categoryType;
    private Context context;
    private int currentRank; // Текущий ранг игрока
    private String temporaryCorrectAnswer; // Временный правильный ответ для кастомных вопросов
    
    // Константы для SharedPreferences
    public static final String PREF_NAME = "MemeoryPrefs";
    public static final String KEY_BEST_STREAK = "best_streak";
    public static final String KEY_LEVEL = "player_level";
    public static final String KEY_BEST_SCORE = "best_score_percentage";
    public static final String KEY_RANK = "player_rank";
    
    // Константы для рангов
    public static final int RANK_SKUF = 0;    // Начальный ранг "Скуф"
    public static final int RANK_ZNATOK = 1;  // Средний ранг "Знаток"
    public static final int RANK_ALTUSKA = 2; // Высший ранг "Альтушка"
    
    // Пороговые значения для повышения ранга
    public static final int STREAK_FOR_ZNATOK = 10;  // Необходимая серия для ранга "Знаток"
    public static final int STREAK_FOR_ALTUSKA = 25; // Необходимая серия для ранга "Альтушка"
    
    /**
     * Создает новый игровой менеджер
     * @param context контекст приложения
     * @param difficulty уровень сложности
     */
    public GameManager(Context context, MemeData.Difficulty difficulty) {
        this(context, difficulty, MemeData.CategoryType.ALL);
    }
    
    /**
     * Создает новый игровой менеджер с выбором категории мемов
     * @param context контекст приложения
     * @param difficulty уровень сложности
     * @param categoryType категория мемов
     */
    public GameManager(Context context, MemeData.Difficulty difficulty, MemeData.CategoryType categoryType) {
        this.context = context;
        this.difficulty = difficulty;
        this.categoryType = categoryType;
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
        currentRank = prefs.getInt(KEY_RANK, RANK_SKUF);
    }
    
    /**
     * Сохраняет статистику пользователя
     */
    private void saveUserStats() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_BEST_STREAK, bestStreak);
        editor.putInt(KEY_LEVEL, currentLevel);
        editor.putInt(KEY_RANK, currentRank);
        
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
        // Получаем исходный список мемов
        memeList = MemeData.getShuffledMemeList(context, difficulty, categoryType);
        
        // Дополнительное перемешивание для более непредсказуемого порядка
        if (memeList != null && memeList.size() > 1) {
            // Эффективно перемешиваем популярные мемы - часто выбираем случайный мем и перемещаем его
            // на другую случайную позицию
            for (int i = 0; i < memeList.size() * 2; i++) {
                int from = (int) (Math.random() * memeList.size());
                int to = (int) (Math.random() * memeList.size());
                if (from != to) {
                    Meme meme = memeList.remove(from);
                    memeList.add(to, meme);
                }
            }
            
            // Намеренно делаем "Толик это подъезд" и другие популярные мемы не первыми в списке
            for (int i = 0; i < memeList.size(); i++) {
                Meme meme = memeList.get(i);
                String name = meme.getCorrectName().toLowerCase();
                if (name.contains("толик") || name.contains("подъезд") || 
                    name.contains("drake") || name.contains("дрейк")) {
                    // Перемещаем эти мемы в середину или конец списка
                    if (i < memeList.size() / 3) {
                        memeList.remove(i);
                        // Вставляем мем в последние 2/3 списка
                        int newPos = memeList.size() / 3 + (int)(Math.random() * (memeList.size() * 2 / 3));
                        if (newPos >= memeList.size()) newPos = memeList.size() - 1;
                        memeList.add(newPos, meme);
                    }
                }
            }
        }
        
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
     * Устанавливает временный правильный ответ для кастомного вопроса
     * @param correctAnswer правильный ответ
     */
    public void setTemporaryCorrectAnswer(String correctAnswer) {
        this.temporaryCorrectAnswer = correctAnswer;
    }
    
    /**
     * Проверяет правильность выбранного ответа
     * @param selectedOption выбранный вариант ответа
     * @return true, если ответ правильный
     */
    public boolean checkAnswer(String selectedOption) {
        totalAttempts++;
        
        // Если есть временный правильный ответ, используем его
        if (temporaryCorrectAnswer != null && !temporaryCorrectAnswer.isEmpty()) {
            boolean isCorrect = temporaryCorrectAnswer.equals(selectedOption);
            temporaryCorrectAnswer = null; // Сбрасываем временный ответ после проверки
            
            if (isCorrect) {
                score++;
                currentStreak++;
                
                // Логика для повышения ранга и сохранения статистики
                if (currentStreak > bestStreak) {
                    bestStreak = currentStreak;
                    
                    // Повышаем уровень каждые 5 единиц серии
                    if (bestStreak % 5 == 0) {
                        currentLevel++;
                    }
                    
                    // Проверяем на повышение ранга
                    updateRank();
                    
                    // Сохраняем новые достижения
                    saveUserStats();
                }
            } else {
                // Сбрасываем текущую серию при неправильном ответе
                currentStreak = 0;
            }
            
            return isCorrect;
        }
        
        // Получаем правильный ответ
        String correctAnswer = getCurrentMeme().getCorrectName();
        
        // Проверяем, если ответ точно совпадает с правильным или содержит его ключевые части
        boolean isCorrect = correctAnswer.equals(selectedOption) || 
                           isPartialMatch(selectedOption, correctAnswer);
        
        if (isCorrect) {
            score++;
            currentStreak++;
            
            // Иногда перемешиваем оставшиеся мемы для большей непредсказуемости
            // Шанс перемешивания увеличивается с каждым правильным ответом
            if (Math.random() < 0.3 + (currentStreak * 0.05)) {
                shuffleRemainingMemes();
            }
            
            // Проверка на рекорд серии
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
                
                // Повышаем уровень каждые 5 единиц серии
                if (bestStreak % 5 == 0) {
                    currentLevel++;
                }
                
                // Проверяем на повышение ранга
                updateRank();
                
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
     * Проверяет, является ли ответ частичным совпадением с правильным
     * (например, "Дрейк" и "Drake (Дрейк)" должны считаться совпадением)
     * @param answer ответ игрока
     * @param correctAnswer правильный ответ
     * @return true если есть частичное совпадение
     */
    private boolean isPartialMatch(String answer, String correctAnswer) {
        // Проверяем, содержит ли правильный ответ выбранный вариант или наоборот
        String lowerAnswer = answer.toLowerCase();
        String lowerCorrect = correctAnswer.toLowerCase();
        
        // Проверяем русские мемы
        if (lowerCorrect.contains("толик") && lowerAnswer.contains("толик") || 
            lowerCorrect.contains("подъезд") && lowerAnswer.contains("подъезд") ||
            lowerCorrect.contains("толян") && lowerAnswer.contains("толян")) {
            return true;
        }
        
        if (lowerCorrect.contains("что если") && lowerAnswer.contains("что если") || 
            lowerCorrect.contains("а что если") && lowerAnswer.contains("а что если")) {
            return true;
        }
        
        if (lowerCorrect.contains("ну давай") && lowerAnswer.contains("ну давай") || 
            lowerCorrect.contains("расскажи") && lowerAnswer.contains("расскажи") ||
            lowerCorrect.contains("вонка") && lowerAnswer.contains("вонка")) {
            return true;
        }
        
        if (lowerCorrect.contains("это поворот") && lowerAnswer.contains("поворот") || 
            lowerCorrect.contains("поворот") && lowerAnswer.contains("это поворот")) {
            return true;
        }
        
        if (lowerCorrect.contains("мать") && lowerAnswer.contains("мать") || 
            lowerCorrect.contains("ебаная") && lowerAnswer.contains("ебаная")) {
            return true;
        }
        
        if (lowerCorrect.contains("происходит") && lowerAnswer.contains("происходит") || 
            lowerCorrect.contains("джеки") && lowerAnswer.contains("джеки") ||
            lowerCorrect.contains("чан") && lowerAnswer.contains("чан")) {
            return true;
        }
        
        // Обновленные проверки для новых мемов
        if (lowerCorrect.contains("chill") && lowerAnswer.contains("chill") || 
            lowerCorrect.contains("чилл") && lowerAnswer.contains("чилл")) {
            return true;
        }
        
        if (lowerCorrect.contains("тиньков") && lowerAnswer.contains("тиньков") || 
            lowerCorrect.contains("оценивает") && lowerAnswer.contains("оценивает")) {
            return true;
        }
        
        if (lowerCorrect.contains("райан") && lowerAnswer.contains("райан") || 
            lowerCorrect.contains("гослинг") && lowerAnswer.contains("гослинг") ||
            lowerCorrect.contains("невозмутимый") && lowerAnswer.contains("невозмутимый")) {
            return true;
        }
        
        if (lowerCorrect.contains("фрай") && lowerAnswer.contains("фрай") || 
            lowerCorrect.contains("недоверчивый") && lowerAnswer.contains("недовер")) {
            return true;
        }
        
        // Проверяем иностранные мемы
        if (lowerCorrect.contains("drake") && lowerAnswer.contains("дрейк") || 
            lowerCorrect.contains("дрейк") && lowerAnswer.contains("drake")) {
            return true;
        }
        
        if (lowerCorrect.contains("distracted") && lowerAnswer.contains("неверный") || 
            lowerCorrect.contains("неверный") && lowerAnswer.contains("distracted")) {
            return true;
        }
        
        if (lowerCorrect.contains("harold") && lowerAnswer.contains("гарольд") || 
            lowerCorrect.contains("гарольд") && lowerAnswer.contains("harold") ||
            lowerCorrect.contains("pain") && lowerAnswer.contains("боль")) {
            return true;
        }
        
        // Для всех других случаев проверяем, содержится ли один ответ в другом
        if (lowerCorrect.contains(lowerAnswer) || lowerAnswer.contains(lowerCorrect)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Обновляет ранг игрока на основе его серии правильных ответов
     */
    private void updateRank() {
        if (bestStreak >= STREAK_FOR_ALTUSKA && currentRank < RANK_ALTUSKA) {
            currentRank = RANK_ALTUSKA;
        } else if (bestStreak >= STREAK_FOR_ZNATOK && currentRank < RANK_ZNATOK) {
            currentRank = RANK_ZNATOK;
        }
    }
    
    /**
     * Переходит к следующему мему
     * @return true, если есть следующий мем, false если достигнут конец списка
     */
    public boolean nextMeme() {
        if (memeList == null || memeList.isEmpty()) {
            return false;
        }
        currentMemeIndex++;
        return currentMemeIndex < memeList.size();
    }
    
    /**
     * @return текущий мем
     */
    public Meme getCurrentMeme() {
        if (memeList == null || memeList.isEmpty() || currentMemeIndex >= memeList.size()) {
            return null;
        }
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
    
    /**
     * @return текущий ранг игрока (RANK_SKUF, RANK_ZNATOK или RANK_ALTUSKA)
     */
    public int getCurrentRank() {
        return currentRank;
    }
    
    /**
     * Возвращает название текущего ранга игрока
     * @return название ранга в виде строки
     */
    public String getCurrentRankName() {
        switch (currentRank) {
            case RANK_ALTUSKA:
                return "Альтушка";
            case RANK_ZNATOK:
                return "Знаток";
            case RANK_SKUF:
            default:
                return "Скуф";
        }
    }
    
    /**
     * Возвращает необходимую серию правильных ответов для достижения следующего ранга
     * @return количество правильных ответов подряд для следующего ранга, или -1 если достигнут максимальный ранг
     */
    public int getStreakForNextRank() {
        switch (currentRank) {
            case RANK_SKUF:
                return STREAK_FOR_ZNATOK;
            case RANK_ZNATOK:
                return STREAK_FOR_ALTUSKA;
            case RANK_ALTUSKA:
            default:
                return -1; // Максимальный ранг уже достигнут
        }
    }
    
    /**
     * Перемешивает оставшиеся мемы в процессе игры
     * Вызывается после нескольких правильных ответов, чтобы избежать запоминания паттернов
     */
    public void shuffleRemainingMemes() {
        if (memeList != null && memeList.size() > currentMemeIndex + 1) {
            // Берем текущий мем отдельно
            Meme currentMeme = null;
            if (currentMemeIndex < memeList.size()) {
                currentMeme = memeList.get(currentMemeIndex);
            }
            
            // Создаем список только оставшихся мемов 
            List<Meme> remainingMemes = new ArrayList<>();
            for (int i = currentMemeIndex + 1; i < memeList.size(); i++) {
                remainingMemes.add(memeList.get(i));
            }
            
            // Перемешиваем оставшиеся мемы
            Collections.shuffle(remainingMemes);
            
            // Обновляем основной список мемов
            for (int i = 0; i < remainingMemes.size(); i++) {
                memeList.set(currentMemeIndex + 1 + i, remainingMemes.get(i));
            }
        }
    }
    
    /**
     * @return текущая категория мемов
     */
    public MemeData.CategoryType getCategoryType() {
        return categoryType;
    }
    
    /**
     * Изменяет категорию мемов и сбрасывает игру
     * @param categoryType новая категория мемов
     */
    public void setCategoryType(MemeData.CategoryType categoryType) {
        this.categoryType = categoryType;
        resetGame();
    }
} 