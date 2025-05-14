package com.kalashianed.memeory;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.kalashianed.memeory.utils.UnsafeOkHttpClient;

import java.io.InputStream;

/**
 * Модуль Glide для добавления настроек и интеграции с OkHttp
 */
@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    private static final String TAG = "MyAppGlideModule";
    private static final int DISK_CACHE_SIZE_BYTES = 1024 * 1024 * 250; // 250 МБ

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        Log.d(TAG, "Применение настроек Glide");
        
        // Увеличиваем размер кэша на диске
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, DISK_CACHE_SIZE_BYTES));
        
        // Устанавливаем уровень логирования
        builder.setLogLevel(Log.VERBOSE);
        
        // Настраиваем глобальные параметры по умолчанию
        RequestOptions requestOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.meme_placeholder)
            .error(R.drawable.meme_placeholder)
            .timeout(30000); // 30 секунд
            
        builder.setDefaultRequestOptions(requestOptions);
        
        Log.d(TAG, "Настройки Glide успешно применены");
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        Log.d(TAG, "Регистрация компонентов Glide");
        
        try {
            // Регистрируем OkHttp клиент для загрузки по URL
            registry.replace(GlideUrl.class, InputStream.class,
                    new OkHttpUrlLoader.Factory(UnsafeOkHttpClient.getUnsafeOkHttpClient()));
            
            Log.d(TAG, "OkHttp клиент успешно зарегистрирован для Glide");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при регистрации компонентов Glide: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false; // Отключаем автоматический парсинг манифеста
    }
} 