package com.kalashianed.memeory.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kalashianed.memeory.generated.GlideApp;
import com.kalashianed.memeory.R;

import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Вспомогательный класс для загрузки изображений с помощью Glide
 */
public class GlideHelper {
    private static final String TAG = "GlideHelper";

    /**
     * Загружает изображение из URL и устанавливает его в ImageView
     * с детальным логированием ошибок
     *
     * @param context контекст приложения
     * @param imageUrl URL изображения
     * @param imageView виджет для отображения изображения
     * @param placeholder ID ресурса заглушки
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, int placeholder) {
        try {
            if (context == null || imageView == null) {
                Log.e(TAG, "Context or ImageView is null");
                return;
            }

            Log.d(TAG, "Loading image from URL: " + imageUrl);
            
            // Отображаем URL в логах для отладки
            Log.d(TAG, "Image URL: " + imageUrl);
            Toast.makeText(context, "Загрузка: " + imageUrl, Toast.LENGTH_SHORT).show();
            
            // Очищаем предыдущие запросы
            Glide.with(context).clear(imageView);
            
            // Добавляем слушатель для обработки ошибок
            RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    String errorMsg = (e != null) ? e.getMessage() : "Unknown error";
                    Log.e(TAG, "Failed to load image: " + errorMsg, e);
                    Toast.makeText(context, "Ошибка загрузки: " + errorMsg, Toast.LENGTH_SHORT).show();
                    
                    // Выводим причины ошибки
                    if (e != null) {
                        for (Throwable t : e.getRootCauses()) {
                            Log.e(TAG, "Root cause: " + t.getMessage(), t);
                        }
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Log.d(TAG, "Image loaded successfully from " + dataSource.name());
                    return false;
                }
            };
            
            // Загружаем изображение через наш GlideApp
            GlideApp.with(context)
                .load(imageUrl)
                .placeholder(placeholder)
                .error(placeholder)
                .listener(requestListener)
                .timeout(30000) // 30 секунд таймаут
                .into(imageView);
            
            Log.d(TAG, "Image loading request submitted");
        } catch (Exception e) {
            Log.e(TAG, "Error in loadImage: " + e.getMessage(), e);
            imageView.setImageResource(placeholder);
            Toast.makeText(context, "Ошибка загрузки изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Очищает кэш Glide
     * 
     * @param context контекст приложения
     */
    public static void clearCache(Context context) {
        try {
            // Очистка кэша должна выполняться в фоновом потоке
            new Thread(() -> {
                try {
                    // Очищаем дисковый кэш в фоновом потоке
                    Glide.get(context).clearDiskCache();
                    
                    // Очистка памяти должна выполняться в основном потоке
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            try {
                                Glide.get(context).clearMemory();
                                Log.d(TAG, "Glide cache cleared successfully");
                            } catch (Exception e) {
                                Log.e(TAG, "Error clearing memory cache: " + e.getMessage(), e);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error clearing disk cache: " + e.getMessage(), e);
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Error starting cache clearing thread: " + e.getMessage(), e);
        }
    }
} 