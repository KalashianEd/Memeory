package com.kalashianed.memeory.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.kalashianed.memeory.R;
import com.kalashianed.memeory.model.Meme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Field;

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
     * Типы категорий для игры
     */
    public enum CategoryType {
        ALL,        // Все мемы
        RUSSIAN,    // Только русские мемы
        ENGLISH,    // Только английские мемы
        MIXED       // Смешанные случайные мемы из разных категорий
    }
    
    /**
     * Инициализирует список мемов для игры
     * @param context контекст приложения для получения строковых ресурсов
     * @return список мемов
     */
    public static List<Meme> getMemeList(Context context) {
        if (memeList == null) {
            memeList = new ArrayList<>();
            Log.d("MemeData", "Инициализация списка мемов");
            initializeMemes(context);
            Log.d("MemeData", "Список мемов инициализирован. Размер: " + memeList.size());
        }
        return memeList;
    }
    
    /**
     * Возвращает перемешанную копию списка мемов для заданной сложности
     * (сохранено для обратной совместимости)
     * @param context контекст приложения
     * @param difficulty уровень сложности
     * @return перемешанный список мемов
     */
    public static List<Meme> getShuffledMemeList(Context context, Difficulty difficulty) {
        return getShuffledMemeList(context, difficulty, CategoryType.ALL);
    }
    
    /**
     * Возвращает перемешанную копию списка мемов для заданной сложности и категории
     * @param context контекст приложения
     * @param difficulty уровень сложности
     * @param categoryType тип категории мемов
     * @return перемешанный список мемов
     */
    public static List<Meme> getShuffledMemeList(Context context, Difficulty difficulty, CategoryType categoryType) {
        List<Meme> allMemes = getMemeList(context);
        Log.d("MemeData", "Получение перемешанного списка мемов по категории: " + categoryType);
        
        // Сначала фильтруем мемы по выбранной категории
        List<Meme> filteredMemes = filterMemesByCategory(allMemes, categoryType);
        
        // Если фильтрация дала пустой список, возвращаемся ко всем мемам
        if (filteredMemes.isEmpty()) {
            Log.d("MemeData", "После фильтрации по категории " + categoryType + " не осталось мемов. Используем все мемы.");
            filteredMemes = new ArrayList<>(allMemes);
        }
        
        Log.d("MemeData", "После фильтрации по категории " + categoryType + " осталось мемов: " + filteredMemes.size());
        
        // Далее действуем как обычно - выбираем мемы согласно сложности
        List<Meme> selectedMemes = new ArrayList<>();
        int memesToSelect;
        
        switch (difficulty) {
            case EASY:
                memesToSelect = 5; // Легкий уровень - 5 мемов
                break;
            case MEDIUM:
                memesToSelect = 10; // Средний уровень - 10 мемов
                break;
            case HARD:
                memesToSelect = filteredMemes.size(); // Сложный уровень - все мемы
                break;
            default:
                memesToSelect = 5;
                break;
        }
        
        // Перемешиваем и выбираем нужное количество мемов
        Collections.shuffle(filteredMemes);
        for (int i = 0; i < Math.min(memesToSelect, filteredMemes.size()); i++) {
            selectedMemes.add(filteredMemes.get(i));
        }
        
        // Дополнительное перемешивание
        Collections.shuffle(selectedMemes);
        Log.d("MemeData", "Создан перемешанный список мемов по категории " + categoryType + 
              ". Размер: " + selectedMemes.size());
        
        return selectedMemes;
    }
    
    /**
     * Фильтрует мемы по выбранной категории
     * @param allMemes полный список мемов
     * @param categoryType тип категории для фильтрации
     * @return отфильтрованный список мемов
     */
    private static List<Meme> filterMemesByCategory(List<Meme> allMemes, CategoryType categoryType) {
        if (categoryType == CategoryType.ALL) {
            return new ArrayList<>(allMemes); // Возвращаем все мемы
        }
        
        List<Meme> result = new ArrayList<>();
        
        if (categoryType == CategoryType.RUSSIAN) {
            // Только русские мемы
            for (Meme meme : allMemes) {
                if (meme.getCategory() == com.kalashianed.memeory.model.MemeCategory.RUSSIAN) {
                    result.add(meme);
                }
            }
        } else if (categoryType == CategoryType.ENGLISH) {
            // Только английские мемы
            for (Meme meme : allMemes) {
                if (meme.getCategory() == com.kalashianed.memeory.model.MemeCategory.ENGLISH) {
                    result.add(meme);
                }
            }
        } else if (categoryType == CategoryType.MIXED) {
            // Смешанные мемы - берем равное количество из каждой категории
            List<Meme> russianMemes = new ArrayList<>();
            List<Meme> englishMemes = new ArrayList<>();
            
            for (Meme meme : allMemes) {
                if (meme.getCategory() == com.kalashianed.memeory.model.MemeCategory.RUSSIAN) {
                    russianMemes.add(meme);
                } else if (meme.getCategory() == com.kalashianed.memeory.model.MemeCategory.ENGLISH) {
                    englishMemes.add(meme);
                }
            }
            
            // Перемешиваем категории
            Collections.shuffle(russianMemes);
            Collections.shuffle(englishMemes);
            
            // Определяем сколько мемов брать из каждой категории (примерно поровну)
            int totalToSelect = Math.min(10, allMemes.size());
            int russianToSelect = Math.min(totalToSelect / 2, russianMemes.size());
            int englishToSelect = Math.min(totalToSelect - russianToSelect, englishMemes.size());
            
            // Добавляем мемы в результат
            for (int i = 0; i < russianToSelect; i++) {
                result.add(russianMemes.get(i));
            }
            
            for (int i = 0; i < englishToSelect; i++) {
                result.add(englishMemes.get(i));
            }
            
            // Если не хватает мемов до нужного количества, добавляем из любой категории
            if (result.size() < totalToSelect) {
                for (Meme meme : allMemes) {
                    if (!result.contains(meme)) {
                        result.add(meme);
                        if (result.size() >= totalToSelect) {
                            break;
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Возвращает список мемов для легкой сложности
     */
    public static List<Meme> getEasyMemes(Context context) {
        List<Meme> allMemes = getMemeList(context);
        List<Meme> easyMemes = new ArrayList<>();
        int count = Math.min(5, allMemes.size());
        
        // Берем первые 5 мемов (или меньше, если их всего меньше)
        for (int i = 0; i < count; i++) {
            easyMemes.add(allMemes.get(i));
        }
        
        return easyMemes;
    }
    
    /**
     * Возвращает список мемов для средней сложности
     */
    public static List<Meme> getMediumMemes(Context context) {
        List<Meme> allMemes = getMemeList(context);
        List<Meme> mediumMemes = new ArrayList<>();
        int count = Math.min(10, allMemes.size());
        
        // Берем первые 10 мемов (или меньше, если их всего меньше)
        for (int i = 0; i < count; i++) {
            mediumMemes.add(allMemes.get(i));
        }
        
        return mediumMemes;
    }
    
    /**
     * Возвращает список мемов для тяжелой сложности (все мемы)
     */
    public static List<Meme> getHardMemes(Context context) {
        return getMemeList(context);
    }
    
    /**
     * Создает и добавляет мемы в список
     */
    private static void initializeMemes(Context context) {
        try {
            if (context == null) {
                Log.e("MemeData", "Ошибка инициализации мемов: context is null");
                return;
            }

            // Отладочный код для вывода всех ресурсов
            Field[] drawableFields = R.drawable.class.getFields();
            for (Field field : drawableFields) {
                Log.d("MemeData", "Доступный drawable ресурс: " + field.getName());
            }

            // Список всех названий мемов (русских и международных)
            List<String> allMemeNames = Arrays.asList(
                context.getString(R.string.tolik_eto_podyezd),
                context.getString(R.string.a_chto_esli),
                context.getString(R.string.nu_davay_rasskazhi),
                context.getString(R.string.vot_eto_povorot),
                context.getString(R.string.zhduun),
                context.getString(R.string.chto_proishodit),
                context.getString(R.string.v_etom_ves_ya),
                context.getString(R.string.ne_smeshite_moi_tapki),
                context.getString(R.string.woman_yelling_at_cat),
                context.getString(R.string.nedoverie_frai),
                context.getString(R.string.drake_hotline_bling),
                context.getString(R.string.distracted_boyfriend),
                context.getString(R.string.hide_pain_harold),
                context.getString(R.string.this_is_fine),
                context.getString(R.string.crying_jordan),
                context.getString(R.string.success_kid),
                context.getString(R.string.doge),
                context.getString(R.string.grumpy_cat),
                context.getString(R.string.bad_luck_brian),
                context.getString(R.string.expanding_brain),
                context.getString(R.string.roll_safe),
                context.getString(R.string.pepe_frog),
                context.getString(R.string.disaster_girl),
                context.getString(R.string.salt_bae),
                context.getString(R.string.coffin_dance),
                context.getString(R.string.one_does_not_simply)
            );

            // Создаем мемы с использованием стандартных имен файлов из папки Memes
            // Английские мемы
            addMeme(context, R.drawable.distractedboyfriend, R.string.distracted_boyfriend, allMemeNames);
            addMeme(context, R.drawable.expandingbrain, R.string.expanding_brain, allMemeNames);
            addMeme(context, R.drawable.disastergirl, R.string.disaster_girl, allMemeNames);
            addMeme(context, R.drawable.garold, R.string.hide_pain_harold, allMemeNames);
            addMeme(context, R.drawable.dogememe, R.string.doge, allMemeNames);
            addMeme(context, R.drawable.sucesskid, R.string.success_kid, allMemeNames);
            addMeme(context, R.drawable.grumpycat, R.string.grumpy_cat, allMemeNames);
            addMeme(context, R.drawable.thinkingmemejpg, R.string.roll_safe, allMemeNames);
            addMeme(context, R.drawable.badluckbrian, R.string.bad_luck_brian, allMemeNames);
            addMeme(context, R.drawable.mjcrying, R.string.crying_jordan, allMemeNames);
            addMeme(context, R.drawable.drakeposting, R.string.drake_hotline_bling, allMemeNames);

            // Русские мемы теперь добавляем напрямую из drawable
            addStandardMeme(context, "a_chto_esli", allMemeNames);
            addStandardMeme(context, "chto_proishodit", allMemeNames);
            addStandardMeme(context, "nedoverie_frai", allMemeNames);
            addStandardMeme(context, "nu_davay_rasskazhi", allMemeNames);
            addStandardMeme(context, "tolik_eto_podyezd", allMemeNames);
            addStandardMeme(context, "v_etom_ves_ya", allMemeNames);
            addStandardMeme(context, "vot_eto_povorot", allMemeNames);
            addStandardMeme(context, "zhduun", allMemeNames);
            addStandardMeme(context, "ne_smeshite_moi_tapki", allMemeNames);
            
            // Остальные мемы
            addStandardMeme(context, "coffin_dance", allMemeNames);
            addStandardMeme(context, "one_does_not_simply", allMemeNames);
            addStandardMeme(context, "pepe_frog", allMemeNames);
            addStandardMeme(context, "salt_bae", allMemeNames);
            addStandardMeme(context, "woman_yelling_at_cat", allMemeNames);

            Log.d("MemeData", "Успешно инициализировано " + memeList.size() + " мемов с локальными изображениями");

        } catch (Exception e) {
            Log.e("MemeData", "Ошибка при инициализации мемов: " + e.getMessage(), e);
        }
    }
    
    /**
     * Добавляет мем в список, используя стандартное имя файла
     */
    private static void addStandardMeme(Context context, String standardName, List<String> allMemeNames) {
        try {
            // Получаем правильное название мема
            String nameKey = standardName.replace('_', '_'); // Сохраняем ключ без изменений
            String correctName = findCorrectName(context, nameKey, allMemeNames);
            
            // Используем новый метод для генерации более сложных вариантов ответов
            List<String> options = generateHarderOptions(correctName, allMemeNames);
            
            // Пробуем несколько способов получения ресурса
            int resourceId = 0;
            
            // Попытка 1: прямо из drawable с тем же именем
            Log.d("MemeData", "Ищем ресурс для мема: " + standardName);
            resourceId = context.getResources().getIdentifier(
                standardName, "drawable", context.getPackageName());
            
            if (resourceId != 0) {
                Log.d("MemeData", "Нашли ресурс: " + standardName + " с ID: " + resourceId);
            } else {
                // Попытка 2: с альтернативным именем
                String altName = getAlternativeName(standardName);
                if (!altName.equals(standardName)) {
                    resourceId = context.getResources().getIdentifier(
                        altName, "drawable", context.getPackageName());
                    if (resourceId != 0) {
                        Log.d("MemeData", "Нашли ресурс по альт. имени: " + altName + " с ID: " + resourceId);
                    }
                }
                
                // Если все еще нет ресурса, используем заглушку
                if (resourceId == 0) {
                    // Пробуем еще одну попытку - с префиксом "memes_"
                    String prefixedName = "memes_" + standardName;
                    resourceId = context.getResources().getIdentifier(
                        prefixedName, "drawable", context.getPackageName());
                    if (resourceId != 0) {
                        Log.d("MemeData", "Нашли ресурс с префиксом memes_: " + prefixedName + " с ID: " + resourceId);
                    } else {
                        Log.e("MemeData", "Не удалось найти ресурс для мема: " + standardName + ", используем заглушку");
                        resourceId = R.drawable.meme_placeholder;
                    }
                }
            }
            
            // Создаем мем с локальным ресурсом
            memeList.add(new Meme(resourceId, correctName, options.toArray(new String[0])));
            
            Log.d("MemeData", "Добавлен мем: " + correctName + " с ресурсом drawable: " + resourceId);
        } catch (Exception e) {
            Log.e("MemeData", "Ошибка при добавлении мема " + standardName + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Возвращает альтернативное имя файла для некоторых мемов
     */
    private static String getAlternativeName(String standardName) {
        switch (standardName) {
            case "vot_eto_povorot":
                return "vot_eto_povorot";
            case "nu_davay_rasskazhi":
                return "nu_davay_rasskazhi";
            case "tolik_eto_podyezd":
                return "tolik_eto_podyezd";
            case "hide_pain_harold":
                return "garold";
            case "hide_the_pain_harold":
                return "garold";
            case "success_kid":
                return "sucesskid";
            case "crying_jordan":
                return "mjcrying";
            case "roll_safe":
                return "thinkingmemejpg";
            case "bad_luck_brian":
                return "badluckbrian";
            case "drake_hotline_bling":
                return "drakeposting";
            case "distracted_boyfriend":
                return "distractedboyfriend";
            case "expanding_brain":
                return "expandingbrain";
            case "disaster_girl":
                return "disastergirl";
            case "doge":
                return "dogememe";
            case "grumpy_cat":
                return "grumpycat";
            case "woman_yelling_at_cat":
                return "woman_yelling_at_cat";
            default:
                return standardName;
        }
    }
    
    /**
     * Находит правильное название мема по ключу
     */
    private static String findCorrectName(Context context, String nameKey, List<String> allMemeNames) {
        // Пытаемся найти ресурс строки с таким же именем
        int stringResId = context.getResources().getIdentifier(
            nameKey, "string", context.getPackageName());
        
        if (stringResId != 0) {
            return context.getString(stringResId);
        }
        
        // Обработка специальных случаев для мемов
        if (nameKey.equals("bad_luck_brian")) {
            return "Bad Luck Brian (Неудачник Брайан)";
        } else if (nameKey.equals("disaster_girl")) {
            return "Disaster Girl (Девочка-катастрофа)";
        } else if (nameKey.equals("distracted_boyfriend")) {
            return "Distracted Boyfriend (Неверный парень)";
        } else if (nameKey.equals("doge")) {
            return "Doge (Собака Doge)";
        } else if (nameKey.equals("drake_hotline_bling")) {
            return "Drake (Дрейк)";
        } else if (nameKey.equals("expanding_brain")) {
            return "Expanding Brain (Расширяющийся мозг)";
        } else if (nameKey.equals("hide_pain_harold")) {
            return "Hide the Pain Harold (Гарольд, скрывающий боль)";
        } else if (nameKey.equals("grumpy_cat")) {
            return "Grumpy Cat (Сердитый кот)";
        } else if (nameKey.equals("crying_jordan")) {
            return "Crying Jordan (Плачущий Джордан)";
        } else if (nameKey.equals("success_kid")) {
            return "Success Kid (Успешный малыш)";
        } else if (nameKey.equals("roll_safe")) {
            return "Roll Safe (Думающий парень)";
        } else if (nameKey.equals("coffin_dance")) {
            return "Coffin Dance (Танцующие с гробом)";
        } else if (nameKey.equals("one_does_not_simply")) {
            return "One Does Not Simply (Боромир/Нельзя просто так)";
        } else if (nameKey.equals("pepe_frog")) {
            return "Pepe the Frog (Лягушонок Пепе)";
        } else if (nameKey.equals("salt_bae")) {
            return "Salt Bae (Солящий повар)";
        } else if (nameKey.equals("woman_yelling_at_cat")) {
            return "Woman Yelling at Cat (Женщина кричит на кота)";
        } else if (nameKey.equals("a_chto_esli")) {
            return "А что если?";
        } else if (nameKey.equals("chto_proishodit")) {
            return "Что происходит?";
        } else if (nameKey.equals("nedoverie_frai")) {
            return "Недоверчивый Фрай";
        } else if (nameKey.equals("ne_smeshite_moi_tapki")) {
            return "Тиньков оценивает";
        } else if (nameKey.equals("nu_davay_rasskazhi")) {
            return "Ну давай, расскажи";
        } else if (nameKey.equals("tolik_eto_podyezd")) {
            return "Толик это подъезд";
        } else if (nameKey.equals("v_etom_ves_ya")) {
            return "Chill Guy (Чилл гай)";
        } else if (nameKey.equals("vot_eto_povorot")) {
            return "Вот это поворот";
        } else if (nameKey.equals("zhduun")) {
            return "Ждун";
        }
        
        // Если строка не найдена, ищем по словам из ключа в списке всех имен
        String searchKey = nameKey.replace('_', ' ').toLowerCase();
        for (String name : allMemeNames) {
            if (name.toLowerCase().contains(searchKey)) {
                return name;
            }
        }
        
        // Если ничего не найдено, возвращаем первое имя из списка или сам ключ
        return allMemeNames.isEmpty() ? nameKey : allMemeNames.get(0);
    }
    
    /**
     * Создает более сложные и похожие варианты ответов для мема
     * @param correctName правильное название мема
     * @param allMemeNames список всех возможных названий мемов
     * @return список вариантов ответов (включая правильный в случайной позиции)
     */
    private static List<String> generateHarderOptions(String correctName, List<String> allMemeNames) {
        List<String> options = new ArrayList<>();
        List<String> otherNames = new ArrayList<>(allMemeNames);
        otherNames.remove(correctName);
        
        // Попытаемся найти похожие варианты по ключевым словам
        List<String> similarOptions = new ArrayList<>();
        String lowerCorrect = correctName.toLowerCase();
        
        // Для популярных мемов добавим часто путаемые варианты
        if (lowerCorrect.contains("толик") || lowerCorrect.contains("подъезд")) {
            // Для мема "Толик это подъезд" часто путают с другими популярными мемами
            for (String name : otherNames) {
                if (name.toLowerCase().contains("дрейк") || 
                    name.toLowerCase().contains("поворот") ||
                    name.toLowerCase().contains("вонка") ||
                    name.toLowerCase().contains("ну давай") ||
                    name.toLowerCase().contains("фрай")) {
                    similarOptions.add(name);
                }
            }
            
            // Если популярный мем в списке первым, вероятность его выбора выше - 
            // создадим искаженные версии правильного ответа
            if (Math.random() < 0.7) {
                String fake1 = "Это Толик и подъезд";
                String fake2 = "Толян это падик";
                
                if (!otherNames.contains(fake1) && !correctName.equals(fake1)) {
                    similarOptions.add(fake1);
                }
                if (!otherNames.contains(fake2) && !correctName.equals(fake2)) {
                    similarOptions.add(fake2);
                }
            }
        } 
        else if (lowerCorrect.contains("drake") || lowerCorrect.contains("дрейк")) {
            for (String name : otherNames) {
                if (name.toLowerCase().contains("толик") || 
                    name.toLowerCase().contains("гарольд") ||
                    name.toLowerCase().contains("harold") ||
                    name.toLowerCase().contains("boyfriend") ||
                    name.toLowerCase().contains("неверный") ||
                    name.toLowerCase().contains("фрай")) {
                    similarOptions.add(name);
                }
            }
            
            // Добавляем искаженные версии
            if (Math.random() < 0.7) {
                String fake1 = "Drake Meme";
                String fake2 = "Дрейк одобряет/не одобряет";
                
                if (!otherNames.contains(fake1) && !correctName.equals(fake1)) {
                    similarOptions.add(fake1);
                }
                if (!otherNames.contains(fake2) && !correctName.equals(fake2)) {
                    similarOptions.add(fake2);
                }
            }
        }
        else if (lowerCorrect.contains("boyfriend") || lowerCorrect.contains("неверный")) {
            for (String name : otherNames) {
                if (name.toLowerCase().contains("дрейк") || 
                    name.toLowerCase().contains("drake") ||
                    name.toLowerCase().contains("мозг") ||
                    name.toLowerCase().contains("brain") ||
                    name.toLowerCase().contains("фрай")) {
                    similarOptions.add(name);
                }
            }
            
            // Добавляем искаженные версии
            if (Math.random() < 0.7) {
                String fake1 = "Отвлёкшийся парень";
                String fake2 = "Парень смотрит на другую";
                
                if (!otherNames.contains(fake1) && !correctName.equals(fake1)) {
                    similarOptions.add(fake1);
                }
                if (!otherNames.contains(fake2) && !correctName.equals(fake2)) {
                    similarOptions.add(fake2);
                }
            }
        }
        else if (lowerCorrect.contains("brain") || lowerCorrect.contains("мозг")) {
            for (String name : otherNames) {
                if (name.toLowerCase().contains("boyfriend") || 
                    name.toLowerCase().contains("неверный") ||
                    name.toLowerCase().contains("фрай") ||
                    name.toLowerCase().contains("drake") ||
                    name.toLowerCase().contains("дрейк")) {
                    similarOptions.add(name);
                }
            }
            
            // Добавляем искаженные версии
            if (Math.random() < 0.7) {
                String fake1 = "Расширяющийся разум";
                String fake2 = "Галактический мозг";
                
                if (!otherNames.contains(fake1) && !correctName.equals(fake1)) {
                    similarOptions.add(fake1);
                }
                if (!otherNames.contains(fake2) && !correctName.equals(fake2)) {
                    similarOptions.add(fake2);
                }
            }
        }
        // Добавляем варианты для Chill Guy
        else if (lowerCorrect.contains("chill") || lowerCorrect.contains("чилл")) {
            for (String name : otherNames) {
                if (name.toLowerCase().contains("гослинг") || 
                    name.toLowerCase().contains("тиньков") ||
                    name.toLowerCase().contains("фрай") ||
                    name.toLowerCase().contains("дрейк")) {
                    similarOptions.add(name);
                }
            }
            
            // Добавляем искаженные версии
            if (Math.random() < 0.7) {
                String fake1 = "Спокойный парень";
                String fake2 = "Chill Dog";
                
                if (!otherNames.contains(fake1) && !correctName.equals(fake1)) {
                    similarOptions.add(fake1);
                }
                if (!otherNames.contains(fake2) && !correctName.equals(fake2)) {
                    similarOptions.add(fake2);
                }
            }
        }
        // Добавляем варианты для Тиньков оценивает
        else if (lowerCorrect.contains("тиньков") || lowerCorrect.contains("оценивает")) {
            for (String name : otherNames) {
                if (name.toLowerCase().contains("чилл") || 
                    name.toLowerCase().contains("гослинг") ||
                    name.toLowerCase().contains("дрейк") ||
                    name.toLowerCase().contains("фрай")) {
                    similarOptions.add(name);
                }
            }
            
            // Добавляем искаженные версии
            if (Math.random() < 0.7) {
                String fake1 = "Олег Тиньков комментирует";
                String fake2 = "Сомнительно, но окей";
                
                if (!otherNames.contains(fake1) && !correctName.equals(fake1)) {
                    similarOptions.add(fake1);
                }
                if (!otherNames.contains(fake2) && !correctName.equals(fake2)) {
                    similarOptions.add(fake2);
                }
            }
        }
        // Добавляем варианты для Невозмутимый Райан Гослинг
        else if (lowerCorrect.contains("гослинг") || lowerCorrect.contains("райан")) {
            for (String name : otherNames) {
                if (name.toLowerCase().contains("чилл") || 
                    name.toLowerCase().contains("тиньков") ||
                    name.toLowerCase().contains("дрейк") ||
                    name.toLowerCase().contains("фрай")) {
                    similarOptions.add(name);
                }
            }
            
            // Добавляем искаженные версии
            if (Math.random() < 0.7) {
                String fake1 = "Хитрый Гэтсби";
                String fake2 = "Драйв";
                
                if (!otherNames.contains(fake1) && !correctName.equals(fake1)) {
                    similarOptions.add(fake1);
                }
                if (!otherNames.contains(fake2) && !correctName.equals(fake2)) {
                    similarOptions.add(fake2);
                }
            }
        }
        else {
            // Для остальных мемов ищем любые похожие
            for (String name : otherNames) {
                String lowerName = name.toLowerCase();
                String[] wordsCorrect = lowerCorrect.split(" ");
                
                // Ищем совпадения по отдельным словам в названии
                for (String word : wordsCorrect) {
                    if (word.length() > 3 && lowerName.contains(word)) {
                        similarOptions.add(name);
                        break;
                    }
                }
            }
        }
        
        // Добавляем похожие варианты если нашли
        Collections.shuffle(similarOptions);
        for (int i = 0; i < Math.min(2, similarOptions.size()); i++) {
            options.add(similarOptions.get(i));
            otherNames.remove(similarOptions.get(i));
        }
        
        // Добавляем оставшиеся случайные варианты до 3
        Collections.shuffle(otherNames);
        for (int i = 0; i < Math.min(3 - options.size(), otherNames.size()); i++) {
            options.add(otherNames.get(i));
        }
        
        // Добавляем правильный ответ в случайную позицию
        int insertPosition = (int) (Math.random() * (options.size() + 1));
        options.add(insertPosition, correctName);
        
        // Дополнительно перемешиваем
        Collections.shuffle(options);
        
        return options;
    }

    /**
     * Добавляет мем, используя прямые ссылки на ресурсы
     */
    private static void addMeme(Context context, int imageResId, int nameResId, List<String> allMemeNames) {
        try {
            // Получаем правильное название мема из ресурсов
            String correctName = context.getString(nameResId);
            
            // Используем новый метод для генерации более сложных вариантов ответов
            List<String> options = generateHarderOptions(correctName, allMemeNames);
            
            // Создаем мем с локальным ресурсом
            memeList.add(new Meme(imageResId, correctName, options.toArray(new String[0])));
            
            Log.d("MemeData", "Добавлен мем напрямую: " + correctName + " с ресурсом drawable ID: " + imageResId);
        } catch (Exception e) {
            Log.e("MemeData", "Ошибка при добавлении мема с ID " + imageResId + ": " + e.getMessage(), e);
        }
    }
} 