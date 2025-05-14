package com.kalashianed.memeory.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.kalashianed.memeory.R;
import com.kalashianed.memeory.data.MemeData;
import com.kalashianed.memeory.model.Meme;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Менеджер для управления локальными файлами мемов
 */
public class MemeCacheManager {
    private static final String TAG = "MemeCacheManager";
    private Context context;
    private OnCacheCompleteListener listener;

    public MemeCacheManager(Context context) {
        this.context = context;
    }

    /**
     * Начинает проверку всех локальных мемов
     * @param listener Слушатель для уведомления о завершении проверки
     */
    public void preloadAllMemes(OnCacheCompleteListener listener) {
        this.listener = listener;
        
        // Получаем все мемы из ресурсов
        List<Meme> allMemes = new ArrayList<>();
        allMemes.addAll(MemeData.getEasyMemes(context));
        allMemes.addAll(MemeData.getMediumMemes(context));
        allMemes.addAll(MemeData.getHardMemes(context));
        
        // Проверяем доступность всех мемов
        int totalMemes = allMemes.size();
        int localMemes = 0;
        
        for (Meme meme : allMemes) {
            if (meme.getImageResId() != 0 && meme.getImageResId() != R.drawable.meme_placeholder) {
                localMemes++;
            }
        }
        
        Log.d(TAG, "Локальных мемов доступно: " + localMemes + " из " + totalMemes);
        Toast.makeText(context, "Локальных мемов: " + localMemes + "/" + totalMemes, Toast.LENGTH_SHORT).show();
        
        // Уведомляем о результате
        if (listener != null) {
            List<String> errors = new ArrayList<>();
            if (localMemes < totalMemes) {
                errors.add("Некоторые мемы недоступны локально");
            }
            listener.onCacheComplete(localMemes == totalMemes, localMemes, totalMemes, errors);
        }
    }

    /**
     * Проверяет наличие всех мемов в локальном хранилище
     * @return true если все мемы доступны локально
     */
    public boolean areAllMemesLocallyCached() {
        List<Meme> allMemes = new ArrayList<>();
        allMemes.addAll(MemeData.getEasyMemes(context));
        allMemes.addAll(MemeData.getMediumMemes(context));
        allMemes.addAll(MemeData.getHardMemes(context));
        
        for (Meme meme : allMemes) {
            if (meme.getImageResId() == 0 || meme.getImageResId() == R.drawable.meme_placeholder) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Удаляет временные файлы из локального хранилища
     * @return количество удаленных файлов
     */
    public int clearMemeCache() {
        File memesDir = new File(context.getCacheDir(), "memes");
        if (!memesDir.exists()) {
            return 0;
        }
        
        int deleted = 0;
        File[] files = memesDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    deleted++;
                }
            }
        }
        
        return deleted;
    }

    /**
     * Получает размер временного кэша мемов в байтах
     * @return размер кэша в байтах или 0, если директория не существует
     */
    public long getMemesCacheSize() {
        File memesDir = new File(context.getCacheDir(), "memes");
        if (!memesDir.exists()) {
            return 0;
        }
        
        long size = 0;
        File[] files = memesDir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += file.length();
            }
        }
        
        return size;
    }

    /**
     * Интерфейс для уведомления о завершении проверки кэша
     */
    public interface OnCacheCompleteListener {
        void onCacheComplete(boolean success, int availableCount, int totalCount, List<String> errors);
    }
} 