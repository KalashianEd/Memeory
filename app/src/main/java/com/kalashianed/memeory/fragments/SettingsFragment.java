package com.kalashianed.memeory.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.kalashianed.memeory.R;
import com.kalashianed.memeory.game.GameManager;

public class SettingsFragment extends Fragment {

    private static final String PREFS_FILE = "memeory_prefs";
    private static final String PREF_SOUND = "sound_enabled";
    private static final String PREF_VIBRATION = "vibration_enabled";
    private static final String PREF_NIGHT_MODE = "night_mode";

    private Switch switchSound;
    private Switch switchVibration;
    private Switch switchNightMode;
    private Button btnResetProgress;
    private SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        preferences = getActivity().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

        // Активация анимированного фона
        View rootView = root.findViewById(R.id.root);
        if (rootView != null && rootView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animDrawable = (AnimationDrawable) rootView.getBackground();
            animDrawable.setEnterFadeDuration(2000);
            animDrawable.setExitFadeDuration(4000);
            animDrawable.start();
        }

        // Инициализация представлений
        TextView tvTitle = root.findViewById(R.id.tvSettingsTitle);
        switchSound = root.findViewById(R.id.switchSound);
        switchVibration = root.findViewById(R.id.switchVibration);
        switchNightMode = root.findViewById(R.id.switchNightMode);
        btnResetProgress = root.findViewById(R.id.btnResetProgress);

        // Применение стиля к переключателям
        applyCustomSwitchStyle();

        // Загрузка сохраненных настроек
        loadSettings();

        // Установка обработчиков событий для переключателей
        setupSwitchListeners();

        // Настройка кнопки сброса прогресса
        setupResetButton();

        return root;
    }

    /**
     * Применяет пользовательский стиль к переключателям
     */
    private void applyCustomSwitchStyle() {
        // Для дополнительной стилизации программно
        switchSound.setThumbTextPadding(8);
        switchVibration.setThumbTextPadding(8);
        switchNightMode.setThumbTextPadding(8);
    }

    /**
     * Устанавливает обработчики событий для переключателей
     */
    private void setupSwitchListeners() {
        switchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSetting(PREF_SOUND, isChecked);
                showFeedback(isChecked ? R.string.sound_enabled : R.string.sound_disabled);
            }
        });

        switchVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSetting(PREF_VIBRATION, isChecked);
                showFeedback(isChecked ? R.string.vibration_enabled : R.string.vibration_disabled);
            }
        });

        switchNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSetting(PREF_NIGHT_MODE, isChecked);
                showFeedback(isChecked ? R.string.night_mode_enabled : R.string.night_mode_disabled);
                // Здесь можно добавить код для переключения темы приложения
            }
        });
    }

    /**
     * Настраивает кнопку сброса прогресса
     */
    private void setupResetButton() {
        btnResetProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Показываем диалог подтверждения
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.reset_progress_title)
                        .setMessage(R.string.reset_progress_confirmation)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            // Сброс всей статистики пользователя
                            resetAllUserStats();
                            // Сброс игры
                            HomeFragment.resetGameState();
                            // Показываем подтверждение
                            showFeedback(R.string.progress_reset_success);
                        })
                        .setNegativeButton(R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    /**
     * Сбрасывает всю статистику пользователя
     */
    private void resetAllUserStats() {
        // Получаем доступ к общим настройкам приложения
        SharedPreferences gamePrefs = getActivity().getSharedPreferences(
                GameManager.PREF_NAME, Context.MODE_PRIVATE);
        
        // Очищаем все настройки, связанные с игрой
        SharedPreferences.Editor editor = gamePrefs.edit();
        editor.putInt(GameManager.KEY_BEST_STREAK, 0);
        editor.putInt(GameManager.KEY_LEVEL, 1);
        editor.putInt(GameManager.KEY_BEST_SCORE, 0);
        editor.apply();
    }

    /**
     * Показывает краткое сообщение пользователю
     */
    private void showFeedback(int stringResId) {
        Toast.makeText(getContext(), stringResId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Загружает сохраненные настройки
     */
    private void loadSettings() {
        switchSound.setChecked(preferences.getBoolean(PREF_SOUND, true));
        switchVibration.setChecked(preferences.getBoolean(PREF_VIBRATION, true));
        switchNightMode.setChecked(preferences.getBoolean(PREF_NIGHT_MODE, false));
    }

    /**
     * Сохраняет настройку
     * @param key ключ настройки
     * @param value значение настройки
     */
    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
} 