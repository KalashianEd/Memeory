package com.kalashianed.memeory;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kalashianed.memeory.fragments.HomeFragment;
import com.kalashianed.memeory.fragments.LeaderboardFragment;
import com.kalashianed.memeory.fragments.ProfileFragment;
import com.kalashianed.memeory.fragments.SettingsFragment;
import com.kalashianed.memeory.utils.UIProportionsManager;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private static final String TAG = "MainActivity";
    private BottomNavigationView navView;
    private UIProportionsManager uiProportionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Очистка кэша Glide для гарантии загрузки свежих изображений
        new Thread(() -> {
            try {
                Glide.get(this).clearDiskCache();
                runOnUiThread(() -> Glide.get(this).clearMemory());
                Log.d(TAG, "Glide cache cleared successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error clearing Glide cache: " + e.getMessage(), e);
            }
        }).start();
        
        // Инициализация менеджера пропорций
        uiProportionsManager = MemeoryApplication.getUIProportionsManager();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Скрываем ActionBar, чтобы убрать верхнюю панель
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Получаем ссылку на BottomNavigationView
        navView = findViewById(R.id.nav_view);
        
        // Применяем пропорциональный дизайн к нижней навигации
        if (navView != null && uiProportionsManager != null) {
            int padding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
            uiProportionsManager.applyProportionalPadding(navView, padding, padding, padding, padding);
            uiProportionsManager.applyProportionalElevation(navView, 8);
        }

        try {
            // Сначала пробуем настроить навигацию стандартным способом
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            
            if (navHostFragment != null) {
                navController = navHostFragment.getNavController();
                
                if (navView != null) {
                    NavigationUI.setupWithNavController(navView, navController);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation with NavController: " + e.getMessage(), e);
            
            // Если стандартный способ не сработал, используем альтернативный
            if (navView != null) {
                navView.setOnNavigationItemSelectedListener(this);
                
                // Загружаем начальный фрагмент
                if (savedInstanceState == null) {
                    navView.setSelectedItemId(R.id.homeFragment);
                }
            } else {
                Toast.makeText(this, "Произошла ошибка при запуске приложения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        
        try {
            if (itemId == R.id.homeFragment) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.leaderboardFragment) {
                selectedFragment = new LeaderboardFragment();
            } else if (itemId == R.id.profileFragment) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.settingsFragment) {
                selectedFragment = new SettingsFragment();
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in manual navigation: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка при переключении экрана", Toast.LENGTH_SHORT).show();
        }
        
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        try {
            return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating up: " + e.getMessage(), e);
            return super.onSupportNavigateUp();
        }
    }
}