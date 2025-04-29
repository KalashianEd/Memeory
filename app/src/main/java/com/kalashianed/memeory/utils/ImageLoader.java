package com.kalashianed.memeory.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.util.Log;

import com.kalashianed.memeory.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Утилита для загрузки изображений из интернета
 */
public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static final Executor executor = Executors.newFixedThreadPool(5);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Загружает изображение из URL и устанавливает его в ImageView
     * @param context контекст приложения
     * @param imageUrl URL изображения
     * @param imageView виджет для отображения изображения
     * @param placeholder заглушка, которая будет показана до загрузки изображения
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, int placeholder) {
        // Устанавливаем placeholder
        imageView.setImageResource(placeholder);
        
        // Сохраняем tag для проверки, что ImageView не переиспользован
        final int tag = System.identityHashCode(imageUrl);
        imageView.setTag(tag);
        
        // Выполняем загрузку в фоновом потоке
        executor.execute(() -> {
            // Загружаем изображение
            Bitmap bitmap = null;
            try {
                Log.d(TAG, "Loading image from URL: " + imageUrl);
                bitmap = loadBitmapFromUrl(imageUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
            }
            
            // Результат загрузки
            final Bitmap finalBitmap = bitmap;
            
            // Обновляем UI в главном потоке
            mainHandler.post(() -> {
                // Проверяем, что ImageView не был переиспользован для другого изображения
                if (imageView.getTag() == null || (int)imageView.getTag() != tag) {
                    Log.d(TAG, "ImageView reused, not setting bitmap");
                    return;
                }
                
                // Устанавливаем изображение или placeholder, если загрузка не удалась
                if (finalBitmap != null) {
                    imageView.setImageBitmap(finalBitmap);
                    Log.d(TAG, "Image successfully loaded and set");
                } else {
                    imageView.setImageResource(placeholder);
                    Log.d(TAG, "Failed to load image, using placeholder");
                }
            });
        });
    }

    /**
     * Загружает Bitmap из URL
     * @param urlString URL изображения
     * @return загруженное изображение или null при ошибке
     */
    private static Bitmap loadBitmapFromUrl(String urlString) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        Bitmap bitmap = null;
        
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HTTP error code: " + responseCode);
                return null;
            }
            
            inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap", e);
            return null;
        } finally {
            // Закрываем соединение и поток
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing connection", e);
            }
        }
    }
} 