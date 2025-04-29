package com.kalashianed.memeory;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kalashianed.memeory.fragments.AccountFragment;
import com.kalashianed.memeory.fragments.HomeFragment;
import com.kalashianed.memeory.fragments.LeaderboardFragment;
import com.kalashianed.memeory.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация нижней навигации
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);

        // Загрузка главного фрагмента при запуске приложения
        loadFragment(new HomeFragment());
    }

    /**
     * Загружает выбранный фрагмент
     * @param fragment фрагмент для загрузки
     * @return true, если фрагмент успешно загружен
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        int id = item.getItemId();
        if (id == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.navigation_leaderboard) {
            fragment = new LeaderboardFragment();
        } else if (id == R.id.navigation_settings) {
            fragment = new SettingsFragment();
        } else if (id == R.id.navigation_account) {
            fragment = new AccountFragment();
        }

        return loadFragment(fragment);
    }
}