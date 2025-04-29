package com.kalashianed.memeory;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kalashianed.memeory.auth.AuthManager;

/**
 * Сплэш-экран, который отображается при запуске приложения
 */
public class SplashActivityNew extends AppCompatActivity {
    
    private static final long SPLASH_DELAY = 2500; // 2.5 секунды
    private AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Инициализация AuthManager
        authManager = AuthManager.getInstance(this);
        
        // Запуск анимированного фона
        View rootLayout = findViewById(R.id.splashRootLayout);
        AnimationDrawable animDrawable = (AnimationDrawable) rootLayout.getBackground();
        animDrawable.setEnterFadeDuration(1000);
        animDrawable.setExitFadeDuration(2000);
        animDrawable.start();
        
        // Анимация для логотипа
        ImageView ivLogo = findViewById(R.id.ivSplashLogo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivLogo.startAnimation(fadeIn);
        
        // Анимация для названия
        TextView tvAppName = findViewById(R.id.tvAppName);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        tvAppName.startAnimation(slideUp);
        
        // Анимация для слогана
        TextView tvAppTagline = findViewById(R.id.tvAppTagline);
        Animation slideUp2 = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideUp2.setStartOffset(400); // Добавляем задержку
        tvAppTagline.startAnimation(slideUp2);
        
        // Задержка перед переходом на следующий экран
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Переход на экран входа или главный экран (если пользователь уже авторизован)
            Intent intent;
            
            if (authManager.isUserLoggedIn()) {
                // Если пользователь уже вошел, сразу направляем на главный экран
                intent = new Intent(SplashActivityNew.this, MainActivity.class);
            } else {
                // Иначе отправляем на экран входа
                intent = new Intent(SplashActivityNew.this, LoginActivity.class);
            }
            
            startActivity(intent);
            finish(); // Закрываем сплэш-экран, чтобы пользователь не мог вернуться на него
            
            // Добавляем анимацию перехода
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DELAY);
    }
} 