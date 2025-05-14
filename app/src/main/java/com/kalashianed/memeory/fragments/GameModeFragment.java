package com.kalashianed.memeory.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.kalashianed.memeory.R;

public class GameModeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_game_mode, container, false);

        // Настраиваем анимированный фон, если он есть
        if (root.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animDrawable = (AnimationDrawable) root.getBackground();
            animDrawable.setEnterFadeDuration(2000);
            animDrawable.setExitFadeDuration(4000);
            animDrawable.start();
        }

        // Настройка кнопок
        Button btnClassicMode = root.findViewById(R.id.btnClassicMode);
        Button btnTimeMode = root.findViewById(R.id.btnTimeMode);
        Button btnChallengeMode = root.findViewById(R.id.btnChallengeMode);
        Button btnBack = root.findViewById(R.id.btnBack);

        // Обработчики нажатий
        btnClassicMode.setOnClickListener(v -> {
            // Временно показываем Toast сообщение
            Toast.makeText(requireContext(), "Классический режим будет доступен в следующем обновлении", Toast.LENGTH_SHORT).show();
        });

        btnTimeMode.setOnClickListener(v -> {
            // Временно показываем Toast сообщение
            Toast.makeText(requireContext(), "Режим на время будет доступен в следующем обновлении", Toast.LENGTH_SHORT).show();
        });

        btnChallengeMode.setOnClickListener(v -> {
            // Временно показываем Toast сообщение
            Toast.makeText(requireContext(), "Режим испытания будет доступен в следующем обновлении", Toast.LENGTH_SHORT).show();
        });

        btnBack.setOnClickListener(v -> {
            // Возвращаемся на предыдущий экран
            Navigation.findNavController(v).navigateUp();
        });

        return root;
    }
} 