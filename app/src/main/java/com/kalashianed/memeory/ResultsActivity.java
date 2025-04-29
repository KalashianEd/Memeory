package com.kalashianed.memeory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kalashianed.memeory.fragments.HomeFragment;

/**
 * Активность для отображения результатов игры
 */
public class ResultsActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_TOTAL = "extra_total";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Получение результатов из Intent
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);

        // Расчет процента правильных ответов
        int percentage = total > 0 ? (score * 100) / total : 0;

        // Инициализация представлений
        TextView tvScore = findViewById(R.id.tvScore);
        TextView tvDetails = findViewById(R.id.tvScoreDetails);
        TextView tvResult = findViewById(R.id.tvResultMessage);
        ImageView ivResult = findViewById(R.id.ivResultEmoji);
        Button btnPlayAgain = findViewById(R.id.btnPlayAgain);
        Button btnShare = findViewById(R.id.btnShare);

        // Установка текста с результатами
        tvScore.setText(percentage + "%");
        tvDetails.setText(getString(R.string.score_details, score, total));

        // Выбор соответствующего сообщения и эмодзи в зависимости от процента
        if (percentage >= 90) {
            tvResult.setText(R.string.result_excellent);
            ivResult.setImageResource(R.drawable.meme_placeholder);
        } else if (percentage >= 70) {
            tvResult.setText(R.string.result_good);
            ivResult.setImageResource(R.drawable.meme_placeholder);
        } else if (percentage >= 50) {
            tvResult.setText(R.string.result_average);
            ivResult.setImageResource(R.drawable.meme_placeholder);
        } else {
            tvResult.setText(R.string.result_poor);
            ivResult.setImageResource(R.drawable.meme_placeholder);
        }

        // Обработчик кнопки "Играть снова"
        btnPlayAgain.setOnClickListener(v -> {
            // Сбрасываем флаг, чтобы начать новую игру
            HomeFragment.resetGameState();
            
            // Возвращаемся на главный экран
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Обработчик кнопки "Поделиться"
        btnShare.setOnClickListener(v -> {
            String shareMessage = getString(R.string.share_message, percentage);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Поделиться через"));
        });
    }
} 