package com.kalashianed.memeory.generated;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.kalashianed.memeory.R;
import com.kalashianed.memeory.utils.UnsafeOkHttpClient;

/**
 * Улучшенный класс-заглушка для GlideApp, который должен генерироваться автоматически.
 * Предоставляет дополнительные методы для загрузки изображений с HTTPS.
 */
public class GlideApp {
    private static final String TAG = "GlideApp";

    /**
     * Возвращает RequestManager для контекста
     */
    public static RequestManager with(Context context) {
        return Glide.with(context);
    }
    
    /**
     * Возвращает RequestManager для View
     */
    public static RequestManager with(android.view.View view) {
        return Glide.with(view);
    }
    
    /**
     * Возвращает RequestManager для фрагмента
     */
    public static RequestManager with(androidx.fragment.app.Fragment fragment) {
        return Glide.with(fragment);
    }
    
    /**
     * Загружает изображение с HTTPS URL с расширенными параметрами для надежности
     */
    public static void loadFromUrl(Context context, String url, ImageView imageView) {
        if (context == null || imageView == null || url == null) {
            Log.e(TAG, "Invalid parameters for loadFromUrl");
            return;
        }
        
        try {
            Log.d(TAG, "Loading image from URL: " + url);
            
            // Явно очищаем предыдущие запросы для ImageView
            Glide.with(context).clear(imageView);
            
            // Создаем RequestListener для логирования ошибок
            RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, 
                                           Target<Drawable> target, boolean isFirstResource) {
                    String errorMsg = (e != null) ? e.getMessage() : "Unknown error";
                    Log.e(TAG, "Failed to load image: " + errorMsg);
                    if (e != null) {
                        for (Throwable t : e.getRootCauses()) {
                            Log.e(TAG, "Root cause: " + t.getMessage(), t);
                        }
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, 
                                            Target<Drawable> target, DataSource dataSource, 
                                            boolean isFirstResource) {
                    Log.d(TAG, "Image loaded successfully from " + dataSource.name());
                    return false;
                }
            };
            
            // Загружаем изображение с надежными параметрами
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.meme_placeholder)
                .error(R.drawable.meme_placeholder)
                .listener(requestListener)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Отключаем кэш для отладки
                .skipMemoryCache(true) // Пропускаем кэш памяти для отладки
                .timeout(30000)  // 30 секунд
                .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage(), e);
            imageView.setImageResource(R.drawable.meme_placeholder);
        }
    }
} 