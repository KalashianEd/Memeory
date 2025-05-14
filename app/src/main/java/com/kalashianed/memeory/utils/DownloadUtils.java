package com.kalashianed.memeory.utils;

import android.content.Context;
import android.util.Log;

/**
 * Вспомогательный класс для работы с локальными файлами мемов
 * (оставлен для обратной совместимости)
 */
public class DownloadUtils {
    private static final String TAG = "DownloadUtils";
    
    /**
     * Этот класс больше не используется для загрузки мемов из интернета
     * Вместо этого мемы хранятся локально в drawable-nodpi/memes
     */
    public static void clearCache() {
        Log.d(TAG, "Метод clearCache вызван, но больше не используется");
    }
    
    /**
     * Интерфейс колбэка для обратной совместимости
     */
    public interface DownloadCallback {
        void onDownloadSuccess(String localPath);
        void onDownloadFailed(String error);
    }
} 