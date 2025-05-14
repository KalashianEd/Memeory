package com.kalashianed.memeory;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kalashianed.memeory.fragments.HomeFragment;
import com.kalashianed.memeory.game.GameManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Активность для отображения результатов игры
 */
public class ResultsActivity extends AppCompatActivity {

    private static final String TAG = "ResultsActivity";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_TOTAL = "extra_total";
    
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private ProgressBar progressBarUpdate;
    private TextView tvUpdateStatus;
    private int percentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        
        // Инициализация Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Получение результатов из Intent
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);

        // Расчет процента правильных ответов
        percentage = total > 0 ? (score * 100) / total : 0;

        // Инициализация представлений
        TextView tvScore = findViewById(R.id.tvScore);
        TextView tvDetails = findViewById(R.id.tvScoreDetails);
        TextView tvResult = findViewById(R.id.tvResultMessage);
        ImageView ivResult = findViewById(R.id.ivResultEmoji);
        Button btnPlayAgain = findViewById(R.id.btnPlayAgain);
        Button btnShare = findViewById(R.id.btnShare);
        
        // Инициализация индикатора обновления рейтинга
        progressBarUpdate = findViewById(R.id.progressBarUpdate);
        tvUpdateStatus = findViewById(R.id.tvUpdateStatus);
        
        if (progressBarUpdate == null) {
            // Если элементы не найдены, создаем их программно
            Log.d(TAG, "Элементы индикатора обновления не найдены в макете, пропускаем отображение");
        }

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
        
        // Обновляем локальные данные
        updateLocalScore(percentage);
        
        // Проверяем, авторизован ли пользователь для обновления Firebase
        if (currentUser != null) {
            // Обновляем данные пользователя в Firebase
            showUpdateProgress(true);
            updateUserScoreInFirebase(percentage);
        } else {
            // Предлагаем пользователю зарегистрироваться для сохранения рейтинга
            showLoginPrompt();
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
    
    /**
     * Показывает диалог с предложением зарегистрироваться
     */
    private void showLoginPrompt() {
        if (isFinishing()) return;
        
        new AlertDialog.Builder(this)
            .setTitle("Сохранить результат?")
            .setMessage("Войдите в аккаунт, чтобы ваш рейтинг отображался в таблице лидеров!")
            .setPositiveButton("Войти", (dialog, which) -> {
                // Здесь можно добавить код для перехода на экран входа
                Toast.makeText(this, "Эта функция будет доступна в следующей версии", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Позже", null)
            .show();
    }
    
    /**
     * Отображает или скрывает индикатор обновления рейтинга
     * @param show true для отображения, false для скрытия
     */
    private void showUpdateProgress(boolean show) {
        if (progressBarUpdate != null && tvUpdateStatus != null) {
            progressBarUpdate.setVisibility(show ? View.VISIBLE : View.GONE);
            tvUpdateStatus.setVisibility(show ? View.VISIBLE : View.GONE);
            tvUpdateStatus.setText(show ? "Обновление рейтинга..." : "Рейтинг обновлен!");
        }
    }
    
    /**
     * Обновляет локальную статистику пользователя
     */
    private void updateLocalScore(int scorePercentage) {
        SharedPreferences prefs = getSharedPreferences(GameManager.PREF_NAME, Context.MODE_PRIVATE);
        int currentBestScore = prefs.getInt(GameManager.KEY_BEST_SCORE, 0);
        
        // Обновляем только если текущий счет лучше предыдущего
        if (scorePercentage > currentBestScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(GameManager.KEY_BEST_SCORE, scorePercentage);
            editor.apply();
            
            Log.d(TAG, "Обновлён локальный рекорд: " + scorePercentage + "%");
        }
    }
    
    /**
     * Обновляет счет пользователя в Firebase
     */
    private void updateUserScoreInFirebase(int scorePercentage) {
        // Проверяем, авторизован ли пользователь
        if (currentUser == null) {
            Log.d(TAG, "Пользователь не авторизован, обновление в Firebase пропущено");
            showUpdateProgress(false);
            return;
        }
        
        Log.d(TAG, "Начало обновления данных в Firebase для пользователя: " + currentUser.getUid());
        
        DocumentReference userRef = firestore.collection("users").document(currentUser.getUid());
        
        // Проверяем наличие коллекции "users"
        firestore.collection("users").limit(1).get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "Проверка коллекции users: " + (querySnapshot.isEmpty() ? "Пуста" : "Имеет документы"));
                
                // Сначала проверяем текущие данные пользователя, чтобы обновить только если новый счет лучше
                userRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Документ пользователя существует: " + documentSnapshot.getData());
                        
                        // Проверяем текущий bestScore в Firestore
                        Integer currentBestScore = 0;
                        if (documentSnapshot.contains("bestScore")) {
                            Object scoreObj = documentSnapshot.get("bestScore");
                            if (scoreObj instanceof Long) {
                                currentBestScore = ((Long) scoreObj).intValue();
                            } else if (scoreObj instanceof Integer) {
                                currentBestScore = (Integer) scoreObj;
                            } else if (scoreObj instanceof Double) {
                                currentBestScore = ((Double) scoreObj).intValue();
                            }
                        }
                        
                        Log.d(TAG, "Текущий счет в Firebase: " + currentBestScore + ", Новый счет: " + scorePercentage);
                        
                        // Обновляем только если новый счет лучше или запись ещё не существует
                        if (scorePercentage > currentBestScore) {
                            // Создаем Map с данными для обновления
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("bestScore", scorePercentage);
                            
                            // Убедимся, что все нужные поля присутствуют
                            if (!documentSnapshot.contains("userId")) {
                                userUpdates.put("userId", currentUser.getUid());
                            }
                            
                            if (!documentSnapshot.contains("username") || documentSnapshot.getString("username") == null) {
                                userUpdates.put("username", currentUser.getDisplayName() != null ? 
                                        currentUser.getDisplayName() : "Пользователь");
                            }
                            
                            if (!documentSnapshot.contains("email") || documentSnapshot.getString("email") == null) {
                                userUpdates.put("email", currentUser.getEmail());
                            }
                            
                            // Получаем данные о серии из SharedPreferences
                            SharedPreferences prefs = getSharedPreferences(GameManager.PREF_NAME, Context.MODE_PRIVATE);
                            int bestStreak = prefs.getInt(GameManager.KEY_BEST_STREAK, 0);
                            
                            // Получаем текущую серию из Firestore
                            Integer currentBestStreak = 0;
                            if (documentSnapshot.contains("bestStreak")) {
                                Object streakObj = documentSnapshot.get("bestStreak");
                                if (streakObj instanceof Long) {
                                    currentBestStreak = ((Long) streakObj).intValue();
                                } else if (streakObj instanceof Integer) {
                                    currentBestStreak = (Integer) streakObj;
                                }
                            }
                            
                            // Обновляем bestStreak, только если локальная серия лучше
                            if (bestStreak > currentBestStreak) {
                                userUpdates.put("bestStreak", bestStreak);
                            }
                            
                            // Обновляем данные в Firestore
                            userRef.set(userUpdates, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Данные успешно обновлены в Firebase");
                                        showUpdateProgress(false);
                                        Toast.makeText(ResultsActivity.this, 
                                                "Ваш рейтинг обновлен!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Ошибка при обновлении данных в Firebase", e);
                                        showUpdateProgress(false);
                                        Toast.makeText(ResultsActivity.this, 
                                                "Ошибка обновления рейтинга: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.d(TAG, "Обновление не требуется, текущий счет лучше");
                            showUpdateProgress(false);
                        }
                    } else {
                        Log.d(TAG, "Документ пользователя не существует, создаем новый");
                        
                        // Документ пользователя не существует, создаем новый
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userId", currentUser.getUid());
                        userData.put("username", currentUser.getDisplayName() != null ? 
                                currentUser.getDisplayName() : "Пользователь");
                        userData.put("email", currentUser.getEmail());
                        userData.put("bestScore", scorePercentage);
                        
                        // Получаем данные о серии из SharedPreferences
                        SharedPreferences prefs = getSharedPreferences(GameManager.PREF_NAME, Context.MODE_PRIVATE);
                        int bestStreak = prefs.getInt(GameManager.KEY_BEST_STREAK, 0);
                        userData.put("bestStreak", bestStreak);
                        
                        // Сохраняем новый документ
                        userRef.set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Новый пользователь добавлен в Firebase");
                                    showUpdateProgress(false);
                                    Toast.makeText(ResultsActivity.this, 
                                            "Ваш профиль создан и рейтинг обновлен!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Ошибка при создании профиля в Firebase", e);
                                    showUpdateProgress(false);
                                    Toast.makeText(ResultsActivity.this, 
                                            "Ошибка создания профиля: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка получения данных пользователя из Firebase", e);
                    showUpdateProgress(false);
                    Toast.makeText(this, "Ошибка подключения к серверу: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Ошибка при проверке коллекции users: " + e.getMessage(), e);
                showUpdateProgress(false);
                Toast.makeText(this, "Ошибка подключения к базе данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
} 