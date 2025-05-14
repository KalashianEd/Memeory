package com.kalashianed.memeory.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kalashianed.memeory.LoginActivity;
import com.kalashianed.memeory.R;
import com.kalashianed.memeory.auth.AuthManager;
import com.kalashianed.memeory.game.GameManager;
import com.kalashianed.memeory.utils.CustomAnimationUtils;

public class ProfileFragment extends Fragment {

    private TextView tvLevel;
    private TextView tvRank;
    private TextView tvRankDescription;
    private TextView tvBestStreak;
    private TextView tvBestScore;
    private ImageView ivRankIcon;
    private ProgressBar pbNextRank;
    private TextView tvNextRankInfo;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Находим все необходимые View
        tvLevel = view.findViewById(R.id.tv_level);
        tvRank = view.findViewById(R.id.tv_rank);
        tvRankDescription = view.findViewById(R.id.tv_rank_description);
        tvBestStreak = view.findViewById(R.id.tv_best_streak);
        tvBestScore = view.findViewById(R.id.tv_best_score);
        ivRankIcon = view.findViewById(R.id.iv_rank_icon);
        pbNextRank = view.findViewById(R.id.pb_next_rank);
        tvNextRankInfo = view.findViewById(R.id.tv_next_rank_info);
        
        // Инициализация кнопки выхода из аккаунта
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Выход из аккаунта
            AuthManager authManager = AuthManager.getInstance(requireContext());
            authManager.signOut();
            navigateToLogin();
        });
        
        // Загружаем и обновляем информацию профиля
        loadProfileInfo();
        
        // Применяем анимации
        animateProfileElements(view);
    }
    
    /**
     * Загружает информацию профиля из SharedPreferences и обновляет UI
     */
    private void loadProfileInfo() {
        // Получаем данные из SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences(GameManager.PREF_NAME, Context.MODE_PRIVATE);
        int level = prefs.getInt(GameManager.KEY_LEVEL, 1);
        int bestStreak = prefs.getInt(GameManager.KEY_BEST_STREAK, 0);
        int bestScore = prefs.getInt(GameManager.KEY_BEST_SCORE, 0);
        int rank = prefs.getInt(GameManager.KEY_RANK, GameManager.RANK_SKUF);
        
        // Устанавливаем данные в UI
        tvLevel.setText(getString(R.string.level_format, level));
        tvBestStreak.setText(getString(R.string.streak_format, bestStreak));
        tvBestScore.setText(getString(R.string.score_percentage_format, bestScore));
        
        // Обновляем информацию о ранге
        updateRankInfo(rank, bestStreak);
    }
    
    /**
     * Обновляет информацию о ранге пользователя
     * @param rank текущий ранг
     * @param bestStreak лучшая серия правильных ответов
     */
    private void updateRankInfo(int rank, int bestStreak) {
        String rankName;
        String rankDescription;
        int rankIcon;
        int nextRankProgress = 0;
        String nextRankInfo = "";
        
        // Определяем имя, описание и иконку ранга
        switch (rank) {
            case GameManager.RANK_ALTUSKA:
                rankName = "Альтушка";
                rankDescription = "Легенда мемов! Вы знаете все мемы и их происхождение.";
                rankIcon = R.drawable.rankaltushka; // Используем JPG иконку ранга
                nextRankInfo = "Максимальный ранг достигнут!";
                nextRankProgress = 100;
                break;
                
            case GameManager.RANK_ZNATOK:
                rankName = "Знаток";
                rankDescription = "Вы хорошо разбираетесь в мемах, но есть куда расти!";
                rankIcon = R.drawable.rankznatok; // Используем JPG иконку ранга
                
                // Рассчитываем прогресс до следующего ранга
                nextRankProgress = Math.min(100, (bestStreak * 100) / GameManager.STREAK_FOR_ALTUSKA);
                nextRankInfo = "До ранга \"Альтушка\": " + (GameManager.STREAK_FOR_ALTUSKA - bestStreak) + 
                              " правильных ответов подряд";
                break;
                
            case GameManager.RANK_SKUF:
            default:
                rankName = "Скуф";
                rankDescription = "Новичок в мире мемов. Вы только начинаете свой путь.";
                rankIcon = R.drawable.rankskuf; // Используем JPG иконку ранга
                
                // Рассчитываем прогресс до следующего ранга
                nextRankProgress = Math.min(100, (bestStreak * 100) / GameManager.STREAK_FOR_ZNATOK);
                nextRankInfo = "До ранга \"Знаток\": " + (GameManager.STREAK_FOR_ZNATOK - bestStreak) + 
                              " правильных ответов подряд";
                break;
        }
        
        // Обновляем UI
        tvRank.setText(rankName);
        tvRankDescription.setText(rankDescription);
        ivRankIcon.setImageResource(rankIcon);
        
        // Обновляем прогресс до следующего ранга
        pbNextRank.setProgress(nextRankProgress);
        tvNextRankInfo.setText(nextRankInfo);
    }
    
    /**
     * Анимирует элементы профиля
     * @param rootView корневой View фрагмента
     */
    private void animateProfileElements(View rootView) {
        // Создаем и запускаем анимации для элементов профиля
        CustomAnimationUtils.fadeIn(tvLevel, 300, 0);
        CustomAnimationUtils.fadeIn(tvRank, 300, 100);
        CustomAnimationUtils.fadeIn(tvRankDescription, 300, 200);
        CustomAnimationUtils.fadeIn(tvBestStreak, 300, 250);
        CustomAnimationUtils.fadeIn(tvBestScore, 300, 300);
        
        // Анимация прогресса
        pbNextRank.setProgress(0);
        CustomAnimationUtils.fadeIn(pbNextRank, 300, 350);
        
        // Запуск анимации прогресса через 500мс
        rootView.postDelayed(() -> {
            int progress = pbNextRank.getProgress();
            pbNextRank.setProgress(0);
            pbNextRank.animate()
                      .setDuration(1000)
                      .withEndAction(() -> pbNextRank.setProgress(progress));
        }, 500);
        
        CustomAnimationUtils.fadeIn(tvNextRankInfo, 300, 400);
    }

    /**
     * Перенаправляет на экран входа
     */
    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
} 