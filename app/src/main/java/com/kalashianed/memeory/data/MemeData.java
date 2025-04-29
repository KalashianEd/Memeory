package com.kalashianed.memeory.data;

import android.content.Context;

import com.kalashianed.memeory.R;
import com.kalashianed.memeory.model.Meme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Класс для управления данными мемов в приложении
 */
public class MemeData {
    
    private static List<Meme> memeList;
    
    /**
     * Уровни сложности игры
     */
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    
    /**
     * Инициализирует список мемов для игры
     * @param context контекст приложения для получения строковых ресурсов
     * @return список мемов
     */
    public static List<Meme> getMemeList(Context context) {
        if (memeList == null) {
            memeList = new ArrayList<>();
            initializeMemes(context);
        }
        return memeList;
    }
    
    /**
     * Возвращает перемешанную копию списка мемов
     * @param context контекст приложения
     * @param difficulty уровень сложности
     * @return перемешанный список мемов
     */
    public static List<Meme> getShuffledMemeList(Context context, Difficulty difficulty) {
        List<Meme> allMemes = getMemeList(context);
        List<Meme> selectedMemes = new ArrayList<>();
        
        // Выбираем количество мемов в зависимости от сложности
        int memesToSelect;
        switch (difficulty) {
            case EASY:
                memesToSelect = 5; // Легкий уровень - 5 мемов
                break;
            case MEDIUM:
                memesToSelect = 10; // Средний уровень - 10 мемов
                break;
            case HARD:
                memesToSelect = allMemes.size(); // Сложный уровень - все мемы
                break;
            default:
                memesToSelect = 5;
                break;
        }
        
        // Выбираем случайные мемы из полного списка
        List<Meme> tempList = new ArrayList<>(allMemes);
        Collections.shuffle(tempList);
        
        for (int i = 0; i < Math.min(memesToSelect, tempList.size()); i++) {
            selectedMemes.add(tempList.get(i));
        }
        
        // Перемешиваем финальный список
        Collections.shuffle(selectedMemes);
        return selectedMemes;
    }
    
    /**
     * Создает и добавляет мемы в список
     */
    private static void initializeMemes(Context context) {
        // Мем 1 - Drake Hotline Bling
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.drake_hotline_bling_url),
                context.getString(R.string.drake_hotline_bling),
                new String[] {
                        context.getString(R.string.drake_hotline_bling),
                        "Нет/Да",
                        "Отвергающий Дрейк",
                        "Дрейк выбирает"
                }
        ));
        
        // Мем 2 - Distracted Boyfriend
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.distracted_boyfriend_url),
                context.getString(R.string.distracted_boyfriend),
                new String[] {
                        context.getString(R.string.distracted_boyfriend),
                        "Парень смотрит на другую",
                        "Измена",
                        "Парень с девушкой"
                }
        ));
        
        // Мем 3 - Expanding Brain
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.expanding_brain_url),
                context.getString(R.string.expanding_brain),
                new String[] {
                        context.getString(R.string.expanding_brain),
                        "Эволюция мозга",
                        "Умный-умнее-гений",
                        "Прогрессия интеллекта"
                }
        ));
        
        // Мем 4 - Disaster Girl
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.disaster_girl_url),
                context.getString(R.string.disaster_girl),
                new String[] {
                        context.getString(R.string.disaster_girl),
                        "Зловещая девочка",
                        "Поджигательница",
                        "Девочка на фоне пожара"
                }
        ));
        
        // Мем 5 - Hide the Pain Harold
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.hide_pain_harold_url),
                context.getString(R.string.hide_pain_harold),
                new String[] {
                        context.getString(R.string.hide_pain_harold),
                        "Скрытая боль",
                        "Улыбка через боль",
                        "Веселый старик"
                }
        ));
        
        // Мем 6 - Doge
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.doge_url),
                context.getString(R.string.doge),
                new String[] {
                        context.getString(R.string.doge),
                        "Такой мем",
                        "Шиба-ину",
                        "Wow такой пес"
                }
        ));
        
        // Мем 7 - Success Kid
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.success_kid_url),
                context.getString(R.string.success_kid),
                new String[] {
                        context.getString(R.string.success_kid),
                        "Ребенок-победитель",
                        "Сжатый кулак",
                        "Малыш-молодец"
                }
        ));
        
        // Мем 8 - Grumpy Cat
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.grumpy_cat_url),
                context.getString(R.string.grumpy_cat),
                new String[] {
                        context.getString(R.string.grumpy_cat),
                        "Угрюмый кот",
                        "Грустный котик",
                        "Недовольный кот"
                }
        ));
        
        // Мем 9 - Thinking Meme / Roll Safe
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.thinking_meme_url),
                context.getString(R.string.roll_safe),
                new String[] {
                        context.getString(R.string.roll_safe),
                        "Палец у виска",
                        "Умный чернокожий",
                        "Хитрый парень"
                }
        ));
        
        // Мем 10 - Bad Luck Brian
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.bad_luck_brian_url),
                context.getString(R.string.bad_luck_brian),
                new String[] {
                        context.getString(R.string.bad_luck_brian),
                        "Невезучий парень",
                        "Фейл",
                        "Печальный студент"
                }
        ));
        
        // Мем 11 - Crying Jordan
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.crying_jordan_url),
                context.getString(R.string.crying_jordan),
                new String[] {
                        context.getString(R.string.crying_jordan),
                        "Майкл Джордан плачет",
                        "Баскетболист в слезах",
                        "Слезы чемпиона"
                }
        ));
        
        // Мем 12 - Pepe Frog
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.pepe_frog_url),
                context.getString(R.string.pepe_frog),
                new String[] {
                        context.getString(R.string.pepe_frog),
                        "Грустная лягушка",
                        "FeelsBadMan",
                        "Лягушонок на диване"
                }
        ));
        
        // Мем 13 - Salt Bae
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.salt_bae_url),
                context.getString(R.string.salt_bae),
                new String[] {
                        context.getString(R.string.salt_bae),
                        "Нусрет Гёкче",
                        "Солящий шеф-повар",
                        "Повар со специями"
                }
        ));
        
        // Мем 14 - This is Fine
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.this_is_fine_url),
                context.getString(R.string.this_is_fine),
                new String[] {
                        context.getString(R.string.this_is_fine),
                        "Собака в огне",
                        "Всё в порядке",
                        "Игнорирование проблем"
                }
        ));
        
        // Мем 15 - One Does Not Simply
        memeList.add(createMeme(
                context,
                R.drawable.meme_placeholder,
                context.getString(R.string.one_does_not_simply_url),
                context.getString(R.string.one_does_not_simply),
                new String[] {
                        context.getString(R.string.one_does_not_simply),
                        "Нельзя просто так взять и...",
                        "Властелин колец мем",
                        "Мем про невозможность"
                }
        ));
    }
    
    /**
     * Создает мем с перемешанными вариантами ответов
     */
    private static Meme createMeme(Context context, int imageResId, String imageUrl, 
                                  String correctName, String[] options) {
        // Перемешиваем варианты ответов для каждого мема
        List<String> optionsList = Arrays.asList(options);
        Collections.shuffle(optionsList);
        
        // Возвращаем мем с перемешанными вариантами
        return new Meme(imageResId, imageUrl, correctName, 
                optionsList.toArray(new String[0]));
    }
} 