package com.kalashianed.memeory;

import android.app.Application;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kalashianed.memeory.utils.UIProportionsManager;
import com.kalashianed.memeory.utils.UnsafeOkHttpClient;

import java.io.InputStream;

import okhttp3.OkHttpClient;

public class MemeoryApplication extends Application {
    private static final String TAG = "MemeoryApplication";
    private static UIProportionsManager uiProportionsManager;
    private static final int MEMORY_CACHE_SIZE_BYTES = 1024 * 1024 * 50; // 50 МБ
    private static final int DISK_CACHE_SIZE_BYTES = 1024 * 1024 * 100;  // 100 МБ

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Инициализация менеджера пропорций
        uiProportionsManager = new UIProportionsManager(this);
        
        // Конфигурация Glide для лучшего производительности и отладки
        configureGlide();
        
        // Устанавливаем небезопасный OkHttpClient для Glide (для HTTPS)
        registerUnsafeOkHttpClient();
        
        // Инициализация Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                // Стандартная инициализация
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase успешно инициализирован");
                
                // Альтернативная инициализация с явными параметрами, если стандартная не работает
                /*
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey("AIzaSyC8DLQkZaGEIE6SgV1b11SpHH1oygjJEUo")
                        .setApplicationId("1:873670028604:android:a7207b02bb73203515c239")
                        .setProjectId("memeoryquiz")
                        .build();
                FirebaseApp.initializeApp(this, options);
                */
            } else {
                Log.d(TAG, "Firebase уже был инициализирован");
            }
            
            // Проверка доступности сервисов Firebase
            FirebaseAuth.getInstance();
            FirebaseFirestore.getInstance();
            Log.d(TAG, "Сервисы Firebase доступны");
        } catch (Exception e) {
            // В случае ошибки, пробуем явную инициализацию
            try {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey("AIzaSyC8DLQkZaGEIE6SgV1b11SpHH1oygjJEUo")
                        .setApplicationId("1:873670028604:android:a7207b02bb73203515c239")
                        .setProjectId("memeoryquiz")
                        .build();
                FirebaseApp.initializeApp(this, options);
                Log.d(TAG, "Firebase инициализирован с явными параметрами");
            } catch (Exception ex) {
                Log.e(TAG, "Ошибка при инициализации Firebase с явными параметрами: " + ex.getMessage(), ex);
            }
            Log.e(TAG, "Ошибка при инициализации Firebase: " + e.getMessage(), e);
        }
        
        Log.d(TAG, "Приложение Memeory запущено");
    }
    
    /**
     * Настраивает библиотеку Glide для оптимальной загрузки изображений
     */
    private void configureGlide() {
        GlideBuilder builder = new GlideBuilder();
        
        // Увеличиваем размер кэша в памяти
        builder.setMemoryCache(new LruResourceCache(MEMORY_CACHE_SIZE_BYTES));
        
        // Увеличиваем размер дискового кэша
        builder.setDiskCache(new InternalCacheDiskCacheFactory(this, DISK_CACHE_SIZE_BYTES));
        
        // Устанавливаем глобальные настройки по умолчанию
        RequestOptions requestOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)  // Кэшируем всё
            .skipMemoryCache(false)                    // Разрешаем кэшировать в памяти
            .placeholder(R.drawable.meme_placeholder)
            .error(R.drawable.meme_placeholder);
            
        builder.setDefaultRequestOptions(requestOptions);
        
        // Включаем подробное логирование для отладки
        builder.setLogLevel(Log.VERBOSE);
        
        // Применяем конфигурацию
        Glide.init(this, builder);
        
        Log.d(TAG, "Glide успешно настроен");
    }
    
    /**
     * Регистрирует небезопасный OkHttpClient для загрузки HTTPS изображений
     */
    private void registerUnsafeOkHttpClient() {
        try {
            // Получаем небезопасный OkHttpClient
            OkHttpClient unsafeOkHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
            
            // Регистрируем его в Glide для загрузки HTTPS изображений
            Glide.get(this).getRegistry().replace(
                GlideUrl.class, 
                InputStream.class, 
                new OkHttpUrlLoader.Factory(unsafeOkHttpClient)
            );
            
            Log.d(TAG, "Установлен небезопасный OkHttpClient для Glide для поддержки HTTPS");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при регистрации небезопасного OkHttpClient: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Очищаем кэш Glide при нехватке памяти
        try {
            Glide.get(this).clearMemory();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при очистке памяти Glide: " + e.getMessage(), e);
        }
    }
    
    /**
     * Получение экземпляра менеджера пропорций
     */
    public static UIProportionsManager getUIProportionsManager() {
        return uiProportionsManager;
    }
} 