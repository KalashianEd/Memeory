package com.kalashianed.memeory.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kalashianed.memeory.R;
import com.kalashianed.memeory.ResultsActivity;
import com.kalashianed.memeory.data.MemeData;
import com.kalashianed.memeory.game.GameManager;
import com.kalashianed.memeory.model.Meme;
import com.kalashianed.memeory.utils.ImageLoader;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private ImageView ivMeme;
    private TextView tvProgress;
    private TextView tvStreak;
    private TextView tvLevel;
    private TextView tvFeedback;
    private Button btnOption1;
    private Button btnOption2;
    private Button btnOption3;
    private Button btnOption4;
    private Button[] optionButtons;
    private Button btnStartGame;
    private RadioGroup rgDifficulty;
    private RadioButton rbEasy;
    private RadioButton rbMedium;
    private RadioButton rbHard;
    private View gameContainer;
    private View startGameContainer;

    private GameManager gameManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MemeData.Difficulty currentDifficulty = MemeData.Difficulty.EASY;
    
    // Анимации для кнопок
    private Animation scaleDownAnim;
    private Animation scaleUpAnim;
    
    // Флаг, указывающий, была ли уже начата игра
    private static boolean isGameStarted = false;
    // Сохраненные состояния игры
    private static GameManager savedGameManager = null;
    private static int savedDifficulty = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Загрузка анимаций
        scaleDownAnim = AnimationUtils.loadAnimation(getContext(), R.anim.button_scale_down);
        scaleUpAnim = AnimationUtils.loadAnimation(getContext(), R.anim.button_scale_up);

        // Активация анимированного фона
        View startContainer = root.findViewById(R.id.startGameContainer);
        AnimationDrawable animDrawable = (AnimationDrawable) startContainer.getBackground();
        animDrawable.setEnterFadeDuration(2000);
        animDrawable.setExitFadeDuration(4000);
        animDrawable.start();
        
        View gameContainer = root.findViewById(R.id.gameContainer);
        AnimationDrawable gameAnimDrawable = (AnimationDrawable) gameContainer.getBackground();
        gameAnimDrawable.setEnterFadeDuration(2000);
        gameAnimDrawable.setExitFadeDuration(4000);
        gameAnimDrawable.start();
        
        // Инициализация представлений
        ivMeme = root.findViewById(R.id.ivMeme);
        tvProgress = root.findViewById(R.id.tvProgress);
        tvStreak = root.findViewById(R.id.tvStreak);
        tvLevel = root.findViewById(R.id.tvLevel);
        tvFeedback = root.findViewById(R.id.tvFeedback);
        btnOption1 = root.findViewById(R.id.btnOption1);
        btnOption2 = root.findViewById(R.id.btnOption2);
        btnOption3 = root.findViewById(R.id.btnOption3);
        btnOption4 = root.findViewById(R.id.btnOption4);
        
        // Инициализация контейнеров
        this.gameContainer = gameContainer;
        this.startGameContainer = startContainer;
        
        // Инициализация элементов выбора сложности
        rgDifficulty = root.findViewById(R.id.rgDifficulty);
        rbEasy = root.findViewById(R.id.rbEasy);
        rbMedium = root.findViewById(R.id.rbMedium);
        rbHard = root.findViewById(R.id.rbHard);
        btnStartGame = root.findViewById(R.id.btnStartGame);
        
        // Инициализация массива кнопок для упрощения работы
        optionButtons = new Button[]{btnOption1, btnOption2, btnOption3, btnOption4};

        // Назначение обработчиков событий для кнопок и сенсорных анимаций
        for (Button button : optionButtons) {
            button.setOnClickListener(this);
            setupButtonAnimation(button);
        }
        
        // Добавляем анимацию для кнопки начала игры
        setupButtonAnimation(btnStartGame);
        
        // Инициализация и настройка кнопок управления в верхней части экрана
        ImageButton btnHome = root.findViewById(R.id.btnHome);
        ImageButton btnRestart = root.findViewById(R.id.btnRestart);
        
        // Кнопка возврата на главный экран
        btnHome.setOnClickListener(v -> {
            // Сбрасываем состояние игры
            resetGameState();
            
            // Возвращаемся к выбору сложности
            gameContainer.setVisibility(View.GONE);
            startGameContainer.setVisibility(View.VISIBLE);
        });
        
        // Кнопка рестарта игры
        btnRestart.setOnClickListener(v -> {
            // Сбрасываем состояние игры, но сохраняем выбранную сложность
            gameManager = new GameManager(requireContext(), currentDifficulty);
            savedGameManager = gameManager;
            savedDifficulty = currentDifficulty.ordinal();
            isGameStarted = true;
            
            // Показываем первый мем
            displayCurrentMeme();
        });
        
        // Загрузка и отображение статистики на главном экране
        loadAndDisplayHomeStats(root);
        
        // Установка слушателя для кнопки начала игры
        btnStartGame.setOnClickListener(v -> {
            // Определение выбранной сложности
            int selectedId = rgDifficulty.getCheckedRadioButtonId();
            if (selectedId == rbEasy.getId()) {
                currentDifficulty = MemeData.Difficulty.EASY;
            } else if (selectedId == rbMedium.getId()) {
                currentDifficulty = MemeData.Difficulty.MEDIUM;
            } else if (selectedId == rbHard.getId()) {
                currentDifficulty = MemeData.Difficulty.HARD;
            }
            
            // Инициализация игры с выбранной сложностью
            gameManager = new GameManager(requireContext(), currentDifficulty);
            savedGameManager = gameManager;
            savedDifficulty = currentDifficulty.ordinal();
            isGameStarted = true;
            
            // Показ игрового экрана
            startGameContainer.setVisibility(View.GONE);
            gameContainer.setVisibility(View.VISIBLE);
            
            // Отображение первого мема
            displayCurrentMeme();
        });
        
        // Проверяем, была ли уже начата игра ранее
        if (isGameStarted && savedGameManager != null) {
            // Восстанавливаем состояние игры
            gameManager = savedGameManager;
            currentDifficulty = MemeData.Difficulty.values()[savedDifficulty];
            
            // Показываем игровой экран вместо экрана выбора сложности
            startGameContainer.setVisibility(View.GONE);
            gameContainer.setVisibility(View.VISIBLE);
            
            // Отображаем текущий мем
            displayCurrentMeme();
        } else {
            // По умолчанию показываем экран выбора сложности
            gameContainer.setVisibility(View.GONE);
            startGameContainer.setVisibility(View.VISIBLE);
        }
        
        return root;
    }

    /**
     * Устанавливает анимацию нажатия для кнопки
     */
    private void setupButtonAnimation(Button button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.startAnimation(scaleDownAnim);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.startAnimation(scaleUpAnim);
                    break;
            }
            // Возвращаем false чтобы событие продолжило обрабатываться OnClickListener
            return false;
        });
    }

    /**
     * Отображает текущий мем и варианты ответов
     */
    private void displayCurrentMeme() {
        // Получение текущего мема
        Meme currentMeme = gameManager.getCurrentMeme();
        
        // Обновление изображения мема
        if (currentMeme.hasImageUrl()) {
            // Если у мема есть URL, загружаем изображение с использованием ImageLoader
            ImageLoader.loadImage(
                requireContext(), 
                currentMeme.getImageUrl(), 
                ivMeme, 
                R.drawable.meme_placeholder
            );
        } else {
            // Иначе используем ресурс из drawable
            ivMeme.setImageResource(currentMeme.getImageResId());
        }
        
        // Обновление прогресса
        tvProgress.setText(gameManager.getProgress());
        
        // Обновление информации о серии правильных ответов и уровне
        updatePlayerStats();
        
        // Обновление вариантов ответов
        String[] options = currentMeme.getOptions();
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(options[i]);
            optionButtons[i].setEnabled(true);
        }
        
        // Скрытие сообщения о результате
        tvFeedback.setVisibility(View.INVISIBLE);
    }

    /**
     * Обновляет информацию о серии правильных ответов и уровне игрока
     */
    private void updatePlayerStats() {
        // Отображаем текущую серию
        tvStreak.setText(getString(R.string.streak_count, gameManager.getCurrentStreak()));
        
        // Отображаем лучшую серию или текущий уровень (в зависимости от настроек)
        tvLevel.setText(getString(R.string.best_streak, gameManager.getBestStreak()));
        
        // Изменение внешнего вида в зависимости от серии ответов
        if (gameManager.getCurrentStreak() >= 5) {
            // Если серия 5 и больше, делаем индикатор более заметным
            tvStreak.setTextColor(getResources().getColor(android.R.color.holo_orange_light, null));
            tvStreak.setTextSize(18);
        } else {
            // Для небольших серий используем стандартный стиль
            tvStreak.setTextColor(getResources().getColor(android.R.color.white, null));
            tvStreak.setTextSize(16);
        }
    }

    @Override
    public void onClick(View v) {
        // Проверка выбранного варианта
        Button clickedButton = (Button) v;
        String selectedOption = clickedButton.getText().toString();
        
        // Проверка ответа
        boolean isCorrect = gameManager.checkAnswer(selectedOption);
        
        // Отображение результата
        showFeedback(isCorrect);
        
        // Изменение цвета кнопки в зависимости от правильности ответа
        if (isCorrect) {
            clickedButton.setBackgroundResource(R.drawable.option_correct_button);
        } else {
            clickedButton.setBackgroundResource(R.drawable.option_wrong_button);
            
            // Находим и подсвечиваем правильный ответ
            String correctAnswer = gameManager.getCurrentMeme().getCorrectName();
            for (Button button : optionButtons) {
                if (button.getText().toString().equals(correctAnswer)) {
                    button.setBackgroundResource(R.drawable.option_correct_button);
                    break;
                }
            }
        }
        
        // Блокировка кнопок, чтобы пользователь не мог нажать несколько раз
        for (Button button : optionButtons) {
            button.setEnabled(false);
        }
        
        // Задержка перед переходом к следующему мему или завершению игры
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToNextQuestion();
            }
        }, 1500); // Задержка 1.5 секунды
    }

    /**
     * Показывает сообщение о правильности ответа
     * @param isCorrect был ли ответ правильным
     */
    private void showFeedback(boolean isCorrect) {
        if (isCorrect) {
            tvFeedback.setText(R.string.correct);
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else {
            String correctName = gameManager.getCurrentMeme().getCorrectName();
            tvFeedback.setText(getString(R.string.wrong, correctName));
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        }
        tvFeedback.setVisibility(View.VISIBLE);
    }

    /**
     * Переходит к следующему вопросу или завершает игру
     */
    private void moveToNextQuestion() {
        boolean hasMoreMemes = gameManager.nextMeme();
        
        if (hasMoreMemes) {
            // Если есть еще мемы, показываем следующий
            displayCurrentMeme();
        } else {
            // Если мемов больше нет, показываем результаты
            showResults();
        }
    }

    /**
     * Переход к экрану результатов
     */
    private void showResults() {
        Intent intent = new Intent(getActivity(), ResultsActivity.class);
        intent.putExtra(ResultsActivity.EXTRA_SCORE, gameManager.getScore());
        intent.putExtra(ResultsActivity.EXTRA_TOTAL, gameManager.getTotalAttempts());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Удаление отложенных задач при уничтожении фрагмента
        handler.removeCallbacksAndMessages(null);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Проверяем, была ли уже начата игра
        if (isGameStarted && savedGameManager != null) {
            // Если да, то продолжаем игру
            gameContainer.setVisibility(View.VISIBLE);
            startGameContainer.setVisibility(View.GONE);
        } else {
            // Если нет, показываем экран выбора сложности
            gameContainer.setVisibility(View.GONE);
            startGameContainer.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Метод для сброса игры, чтобы начать сначала
     */
    public static void resetGameState() {
        isGameStarted = false;
        savedGameManager = null;
        savedDifficulty = -1;
    }

    /**
     * Загружает и отображает статистику пользователя на главном экране
     */
    private void loadAndDisplayHomeStats(View root) {
        // Получаем сохраненную статистику
        SharedPreferences prefs = requireContext().getSharedPreferences(GameManager.PREF_NAME, Context.MODE_PRIVATE);
        int bestStreak = prefs.getInt(GameManager.KEY_BEST_STREAK, 0);
        int bestScore = prefs.getInt("best_score_percentage", 0);
        
        // Отображаем статистику
        TextView tvBestScore = root.findViewById(R.id.tvBestScore);
        TextView tvBestStreakHome = root.findViewById(R.id.tvBestStreakHome);
        
        tvBestScore.setText(bestScore + "%");
        tvBestStreakHome.setText(bestStreak + " 🔥");
    }
} 