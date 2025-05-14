package com.kalashianed.memeory.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kalashianed.memeory.generated.GlideApp;
import com.kalashianed.memeory.R;
import com.kalashianed.memeory.ResultsActivity;
import com.kalashianed.memeory.data.MemeData;
import com.kalashianed.memeory.game.GameManager;
import com.kalashianed.memeory.model.Meme;
import com.kalashianed.memeory.utils.CustomAnimationUtils;
import com.kalashianed.memeory.utils.FragmentStyleHelper;

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
    private ImageView ivGameLogo;
    private TextView tvGameTitleStart;
    private TextView tvTagline;
    private TextView tvSelectDifficulty;
    private Button btnPlay;
    private Button btnLeaderboard;
    private Button btnProfile;
    private TextView tvWelcome;
    private TextView tvCurrentRank;
    private ImageView ivCurrentRank;
    private ImageView ivRankBadge;
    private TextView tvQuestion;

    // Константы для навигации
    private static final String ACTION_HOME_TO_GAME = "action_homeFragment_to_gameModeFragment";
    private static final String ACTION_HOME_TO_LEADERBOARD = "action_homeFragment_to_leaderboardFragment";
    private static final String ACTION_HOME_TO_PROFILE = "action_homeFragment_to_profileFragment";

    private GameManager gameManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MemeData.Difficulty currentDifficulty = MemeData.Difficulty.EASY;
    private MemeData.CategoryType currentCategory = MemeData.CategoryType.ALL;
    
    // Анимации для кнопок
    private Animation scaleDownAnim;
    private Animation scaleUpAnim;
    private Animation fadeInAnim;
    private Animation slideUpAnim;
    
    // Флаг, указывающий, была ли уже начата игра
    private static boolean isGameStarted = false;
    // Сохраненные состояния игры
    private static GameManager savedGameManager = null;
    private static int savedDifficulty = -1;
    private static int savedCategory = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Применяем пропорциональные стили
        FragmentStyleHelper.applyStylesToFragment(this, root);

        // Загрузка анимаций
        scaleDownAnim = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.button_scale_down);
        scaleUpAnim = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.button_scale_up);
        fadeInAnim = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        slideUpAnim = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_with_fade);

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
        
        // Инициализация контейнеров
        this.gameContainer = gameContainer;
        this.startGameContainer = startContainer;
        
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
        
        // Инициализация элементов экрана выбора сложности
        rgDifficulty = root.findViewById(R.id.rgDifficulty);
        rbEasy = root.findViewById(R.id.rbEasy);
        rbMedium = root.findViewById(R.id.rbMedium);
        rbHard = root.findViewById(R.id.rbHard);
        
        btnStartGame = root.findViewById(R.id.btnStartGame);
        ivGameLogo = root.findViewById(R.id.ivGameLogo);
        tvGameTitleStart = root.findViewById(R.id.tvGameTitleStart);
        tvTagline = root.findViewById(R.id.tvTagline);
        tvSelectDifficulty = root.findViewById(R.id.tvSelectDifficulty);
        tvQuestion = root.findViewById(R.id.tvQuestion);
        
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
            
            // Возвращаемся к выбору сложности с анимацией
            gameContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    gameContainer.setVisibility(View.GONE);
                    startGameContainer.setVisibility(View.VISIBLE);
                    startGameContainer.setAlpha(0f);
                    startGameContainer.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
                    
                    // Анимация элементов стартового экрана
                    animateStartScreenElements();
                    
                    // Показываем верхние кнопки и приветствие
                    if (tvWelcome != null) tvWelcome.setVisibility(View.VISIBLE);
                    if (tvCurrentRank != null) tvCurrentRank.setVisibility(View.VISIBLE);
                    if (ivCurrentRank != null) ivCurrentRank.setVisibility(View.VISIBLE);
                    if (ivRankBadge != null) ivRankBadge.setVisibility(View.VISIBLE);
                    if (btnPlay != null) btnPlay.setVisibility(View.VISIBLE);
                    if (btnLeaderboard != null) btnLeaderboard.setVisibility(View.VISIBLE);
                    if (btnProfile != null) btnProfile.setVisibility(View.VISIBLE);
                })
                .start();
        });
        
        // Кнопка рестарта игры
        btnRestart.setOnClickListener(v -> {
            // Сбрасываем состояние игры, но сохраняем выбранную сложность и категорию
            gameManager = new GameManager(requireContext(), currentDifficulty, currentCategory);
            savedGameManager = gameManager;
            savedDifficulty = currentDifficulty.ordinal();
            savedCategory = currentCategory.ordinal();
            isGameStarted = true;
            
            // Переключение анимации фона
            gameContainer.setPressed(true);
            gameContainer.setPressed(false);
            
            // Показываем первый мем с анимацией
            CustomAnimationUtils.fadeAnimation(ivMeme, 1.0f, 0.0f, 200);
            handler.postDelayed(() -> {
                displayCurrentMeme(); 
                CustomAnimationUtils.fadeAnimation(ivMeme, 0.0f, 1.0f, 300);
                
                // Анимируем кнопки вариантов
                animateOptionButtons();
            }, 250);
        });
        
        // Настраиваем обработчик нажатия на кнопку "Начать игру"
        btnStartGame.setOnClickListener(v -> {
            // Определяем выбранную сложность
            int selectedId = rgDifficulty.getCheckedRadioButtonId();
            if (selectedId == rbEasy.getId()) {
                currentDifficulty = MemeData.Difficulty.EASY;
            } else if (selectedId == rbMedium.getId()) {
                currentDifficulty = MemeData.Difficulty.MEDIUM;
            } else if (selectedId == rbHard.getId()) {
                currentDifficulty = MemeData.Difficulty.HARD;
            }
            
            // Всегда используем категорию ALL
            currentCategory = MemeData.CategoryType.ALL;
            
            // Инициализируем игровой менеджер с выбранной сложностью
            gameManager = new GameManager(requireContext(), currentDifficulty, currentCategory);
            savedGameManager = gameManager;
            savedDifficulty = currentDifficulty.ordinal();
            savedCategory = currentCategory.ordinal();
            isGameStarted = true;
            
            // Скрываем элементы заголовка (приветствие и кнопки навигации)
            if (tvWelcome != null) tvWelcome.setVisibility(View.GONE);
            if (tvCurrentRank != null) tvCurrentRank.setVisibility(View.GONE);
            if (ivCurrentRank != null) ivCurrentRank.setVisibility(View.GONE);
            if (ivRankBadge != null) ivRankBadge.setVisibility(View.GONE);
            if (btnPlay != null) btnPlay.setVisibility(View.GONE);
            if (btnLeaderboard != null) btnLeaderboard.setVisibility(View.GONE);
            if (btnProfile != null) btnProfile.setVisibility(View.GONE);
            
            // Переключение анимации фона
            startGameContainer.setPressed(true);
            startGameContainer.setPressed(false);
            
            // Переключаемся на игровой экран с анимацией
            startGameContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    startGameContainer.setVisibility(View.GONE);
                    gameContainer.setVisibility(View.VISIBLE);
                    gameContainer.setAlpha(0f);
                    gameContainer.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            // Отображаем первый мем и анимируем элементы игрового экрана
                            displayCurrentMeme();
                            animateGameElements();
                        })
                        .start();
                })
                .start();
        });
        
        // Загрузка и отображение статистики на главном экране
        loadAndDisplayHomeStats(root);
        
        // Находим все необходимые View
        btnPlay = root.findViewById(R.id.btnPlay);
        btnLeaderboard = root.findViewById(R.id.btnLeaderboard);
        btnProfile = root.findViewById(R.id.btnProfile);
        tvWelcome = root.findViewById(R.id.tv_welcome);
        tvCurrentRank = root.findViewById(R.id.tv_current_rank);
        ivCurrentRank = root.findViewById(R.id.iv_current_rank);
        ivRankBadge = root.findViewById(R.id.iv_rank_badge);
        
        // Загружаем информацию о пользователе
        loadUserInfo();
        
        // Настраиваем обработчики нажатий
        setupClickListeners(root);
        
        // Анимируем элементы
        animateElements();
        
        // Установка начального состояния видимости элементов
        if (isGameStarted && savedGameManager != null) {
            gameContainer.setVisibility(View.VISIBLE);
            startGameContainer.setVisibility(View.GONE);
            
            // Напрямую устанавливаем видимость элементов
            if (tvWelcome != null) tvWelcome.setVisibility(View.GONE);
            if (tvCurrentRank != null) tvCurrentRank.setVisibility(View.GONE);
            if (ivCurrentRank != null) ivCurrentRank.setVisibility(View.GONE);
            if (btnPlay != null) btnPlay.setVisibility(View.GONE);
            if (btnLeaderboard != null) btnLeaderboard.setVisibility(View.GONE);
            if (btnProfile != null) btnProfile.setVisibility(View.GONE);
        } else {
            gameContainer.setVisibility(View.GONE);
            startGameContainer.setVisibility(View.VISIBLE);
            
            // Элементы верхней части уже видимы по умолчанию, ничего не нужно делать
        }
        
        return root;
    }
    
    /**
     * Анимирует элементы на стартовом экране выбора сложности
     */
    private void animateStartScreenElements() {
        // Убедимся, что getView() не возвращает null
        View rootView = getView();
        if (rootView == null) return;
        
        // Анимация появления лого
        if (ivGameLogo != null) {
            CustomAnimationUtils.applyScaleInAnimation(ivGameLogo, 0, 400);
        }
        
        // Анимация появления заголовка и подзаголовка
        if (tvGameTitleStart != null) {
            CustomAnimationUtils.applySlideUpWithFade(tvGameTitleStart, 300, 500);
        }
        
        if (tvTagline != null) {
            CustomAnimationUtils.applySlideUpWithFade(tvTagline, 450, 500);
        }
        
        // Анимация появления текста выбора сложности
        if (tvSelectDifficulty != null) {
            CustomAnimationUtils.applySlideUpWithFade(tvSelectDifficulty, 600, 500);
        }
        
        // Анимация появления контейнера с радио-кнопками
        View cvDifficulty = rootView.findViewById(R.id.cvDifficultyContainer);
        if (cvDifficulty != null) {
            CustomAnimationUtils.applySlideUpWithFade(cvDifficulty, 750, 500);
        }
        
        // Пульсирующая анимация кнопки начала игры
        if (btnStartGame != null) {
            handler.postDelayed(() -> {
                if (btnStartGame != null && btnStartGame.getContext() != null) {
                    CustomAnimationUtils.applyScaleInAnimation(btnStartGame, 0, 400);
                    handler.postDelayed(() -> {
                        if (btnStartGame != null && btnStartGame.getContext() != null) {
                            CustomAnimationUtils.applyPulseAnimation(btnStartGame, true);
                        }
                    }, 600);
                }
            }, 1050);
        }
    }
    
    /**
     * Анимирует элементы на игровом экране
     */
    private void animateGameElements() {
        // Убедимся, что view не null
        if (getView() == null) return;
        
        // Анимация появления мема
        if (ivMeme != null) {
            CustomAnimationUtils.applyScaleInAnimation(ivMeme, 0, 400);
        }
        
        // Анимация появления прогресса и уровня
        if (tvProgress != null) {
            CustomAnimationUtils.applySlideInFromLeft(tvProgress, 200, 400);
        }
        
        if (tvLevel != null) {
            CustomAnimationUtils.applySlideInFromRight(tvLevel, 200, 400);
        }
        
        // Анимация появления серии
        if (tvStreak != null) {
            CustomAnimationUtils.applySlideUpWithFade(tvStreak, 400, 400);
        }
        
        // Анимация появления кнопок вариантов
        animateOptionButtons();
    }
    
    /**
     * Анимирует кнопки вариантов ответов по принципу каскада
     */
    private void animateOptionButtons() {
        if (optionButtons == null) return;
        
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i] != null) {
                int delay = 300 + (i * 100);
                CustomAnimationUtils.applySlideUpWithFade(optionButtons[i], delay, 400);
            }
        }
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
     * Отображает текущий мем на экране
     */
    private void displayCurrentMeme() {
        // Получаем текущий мем
        Meme currentMeme = gameManager.getCurrentMeme();
        if (currentMeme == null) {
            // Если текущий мем не найден, показываем сообщение об ошибке
            Toast.makeText(requireContext(), "Ошибка: мем не найден", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Загружаем изображение мема
            int imageResId = currentMeme.getImageResId();
            ivMeme.setImageResource(imageResId);
            
            // Применяем анимацию к изображению
            ivMeme.setAlpha(0f);
            ivMeme.setScaleX(0.85f);
            ivMeme.setScaleY(0.85f);
            ivMeme.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
            
            // Получаем имя мема в формате ключа
            String memeName = currentMeme.getCorrectName().toLowerCase()
                    .replace(" ", "_")
                    .replace("(", "")
                    .replace(")", "")
                    .replace("-", "_");
            
            Log.d("HomeFragment", "Мем: " + currentMeme.getCorrectName() + ", ключ: " + memeName);
            
            // Определяем нужные вопросы и ответы в зависимости от мема
            String question = "";
            String[] options = new String[4];
            String correctOption = "";
            
            // Русские мемы
            if (memeName.contains("a_chto_esli") || memeName.contains("а_что_если")) {
                question = "Что делает персонаж в меме «А что если?»?";
                options[0] = "Спрашивает очевидное";
                options[1] = "Предполагает нелогичную, но забавную идею";
                options[2] = "Делает серьезное заявление";
                options[3] = "Удивляется происходящему";
                correctOption = options[1]; // Б
            } 
            else if (memeName.contains("chto_proishodit") || memeName.contains("что_происходит")) {
                question = "В каком контексте обычно используется мем «Что происходит?»";
                options[0] = "Когда всё идёт по плану";
                options[1] = "При неожиданной и абсурдной ситуации";
                options[2] = "Когда ничего не меняется";
                options[3] = "При просмотре новостей";
                correctOption = options[1]; // Б
            }
            else if (memeName.contains("nedoverie_frai") || memeName.contains("фрай")) {
                question = "Какую эмоцию выражает \"Недоверчивый Фрай\"?";
                options[0] = "Радость";
                options[1] = "Злость";
                options[2] = "Сомнение и подозрение";
                options[3] = "Удивление";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("ne_smeshite_moi_tapki") || memeName.contains("тиньков")) {
                question = "Что делает Олег Тиньков в меме «Тиньков оценивает»?";
                options[0] = "Рассказывает анекдоты";
                options[1] = "Даёт финансовые советы";
                options[2] = "Сравнивает вещи и выносит субъективную оценку";
                options[3] = "Критикует конкурентов";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("nu_davay_rasskazhi") || memeName.contains("ну_давай")) {
                question = "Что выражает фраза «Ну давай, расскажи» в мемах?";
                options[0] = "Искренний интерес";
                options[1] = "Усталость от спора";
                options[2] = "Агрессию";
                options[3] = "Саркастическое ожидание глупого объяснения";
                correctOption = options[3]; // Г
            }
            else if (memeName.contains("tolik_eto_podyezd") || memeName.contains("толик")) {
                question = "Что обычно подразумевается под фразой «Толик, это подъезд»?";
                options[0] = "Толик не туда пошёл";
                options[1] = "Шутка о странной логике";
                options[2] = "Абсурдная сцена из видеозаписи";
                options[3] = "Призыв к порядку";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("v_etom_ves_ya") || memeName.contains("чилл") || memeName.contains("chill")) {
                question = "Что делает персонаж в меме «Чилл гай»?";
                options[0] = "Панически реагирует";
                options[1] = "Убегает от опасности";
                options[2] = "Спокойно принимает любую ситуацию";
                options[3] = "Злится на происходящее";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("vot_eto_povorot") || memeName.contains("поворот")) {
                question = "Когда используется фраза «Вот это поворот»?";
                options[0] = "При предсказуемой ситуации";
                options[1] = "Когда что-то идёт по плану";
                options[2] = "При неожиданной развязке";
                options[3] = "Когда всё остаётся без изменений";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("zhduun") || memeName.contains("ждун")) {
                question = "Что символизирует мем «Ждун»?";
                options[0] = "Человека, который торопится";
                options[1] = "Ожидание с терпением и безысходностью";
                options[2] = "Радость от праздника";
                options[3] = "Грусть из-за одиночества";
                correctOption = options[1]; // Б
            }
            // Английские мемы
            else if (memeName.contains("bad_luck_brian") || memeName.contains("брайан")) {
                question = "Что обычно изображает мем с \"Неудачником Брайаном\"?";
                options[0] = "Удачные совпадения";
                options[1] = "Парадоксы логики";
                options[2] = "Курьезные неудачи";
                options[3] = "Победы в соревнованиях";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("disaster_girl") || memeName.contains("девочка-катастрофа")) {
                question = "Почему \"Девочка-катастрофа\" улыбается на фоне горящего дома?";
                options[0] = "Она поджигатель";
                options[1] = "Это постановочное фото с сюрпризом";
                options[2] = "Её радость — часть контраста ироничного мема";
                options[3] = "Дом её врагов";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("distracted_boyfriend") || memeName.contains("неверный_парень")) {
                question = "Что символизируют персонажи на меме \"Неверный парень\"?";
                options[0] = "Треугольник любовных отношений";
                options[1] = "Невнимательность на работе";
                options[2] = "Выбор между старым и новым";
                options[3] = "Конфликт друзей";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("doge") || memeName.contains("собака_doge")) {
                question = "Какой стиль речи используется в меме Doge?";
                options[0] = "Высокий стиль с архаизмами";
                options[1] = "Ломаный английский с простыми выражениями";
                options[2] = "Научный стиль";
                options[3] = "Поэтический язык";
                correctOption = options[1]; // Б
            }
            else if (memeName.contains("drake") || memeName.contains("дрейк")) {
                question = "Что делает Дрейк в популярном меме?";
                options[0] = "Танцует под свою песню";
                options[1] = "Сравнивает два варианта, отвергая один и принимая другой";
                options[2] = "Улыбается на фоне";
                options[3] = "Поднимает тост";
                correctOption = options[1]; // Б
            }
            else if (memeName.contains("expanding_brain") || memeName.contains("расширяющийся_мозг")) {
                question = "Что обозначает увеличение мозга в этом меме?";
                options[0] = "Рост интеллекта";
                options[1] = "Увеличение гнева";
                options[2] = "Усталость";
                options[3] = "Боль";
                correctOption = options[0]; // А
            }
            else if (memeName.contains("hide_pain_harold") || memeName.contains("гарольд")) {
                question = "Что символизирует выражение лица Гарольда?";
                options[0] = "Радость";
                options[1] = "Подозрение";
                options[2] = "Вежливую грусть";
                options[3] = "Скрытую боль за улыбкой";
                correctOption = options[3]; // Г
            }
            else if (memeName.contains("grumpy_cat") || memeName.contains("сердитый_кот")) {
                question = "Почему Grumpy Cat стал популярен?";
                options[0] = "Он мяукает на камеру";
                options[1] = "Он выглядит постоянно сердитым";
                options[2] = "Он умеет говорить";
                options[3] = "Его спасли с улицы";
                correctOption = options[1]; // Б
            }
            else if (memeName.contains("crying_jordan") || memeName.contains("плачущий_джордан")) {
                question = "Как используется лицо плачущего Майкла Джордана?";
                options[0] = "Чтобы показать грусть и поражение";
                options[1] = "Чтобы выразить радость";
                options[2] = "В контексте спортивных побед";
                options[3] = "Для пародии на фильмы";
                correctOption = options[0]; // А
            }
            else if (memeName.contains("success_kid") || memeName.contains("успешный_малыш")) {
                question = "Что изображает мем с \"Успешным малышом\"?";
                options[0] = "Малыша, уронившего игрушку";
                options[1] = "Ребенка, который плачет";
                options[2] = "Малыша с жестом победы";
                options[3] = "Спящего младенца";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("roll_safe") || memeName.contains("думающий_парень")) {
                question = "Что символизирует жест у виска в этом меме?";
                options[0] = "Страх";
                options[1] = "Умный, но ошибочный ход";
                options[2] = "Ловкий трюк";
                options[3] = "Саркастичное \"умное\" решение";
                correctOption = options[3]; // Г
            }
            else if (memeName.contains("coffin_dance") || memeName.contains("танцующие_с_гробом")) {
                question = "Откуда появился мем \"Танцующие с гробом\"?";
                options[0] = "Из европейского клипа";
                options[1] = "С похорон в Гане";
                options[2] = "Из компьютерной игры";
                options[3] = "Из сериала";
                correctOption = options[1]; // Б
            }
            else if (memeName.contains("one_does_not_simply") || memeName.contains("боромир") || memeName.contains("нельзя_просто_так")) {
                question = "Из какого фильма сцена с \"One does not simply walk into Mordor\"?";
                options[0] = "Гарри Поттер";
                options[1] = "Игра престолов";
                options[2] = "Властелин колец";
                options[3] = "Хоббит";
                correctOption = options[2]; // В
            }
            else if (memeName.contains("pepe") || memeName.contains("пепе") || memeName.contains("лягушонок")) {
                question = "Как изначально появился лягушонок Пепе?";
                options[0] = "Как персонаж комиксов";
                options[1] = "Как герой мультфильма";
                options[2] = "Как эмодзи";
                options[3] = "Как персонаж видеоигры";
                correctOption = options[0]; // А
            }
            else if (memeName.contains("salt_bae") || memeName.contains("солящий_повар")) {
                question = "Чем прославился повар Salt Bae?";
                options[0] = "Готовкой пиццы";
                options[1] = "Уникальным способом посыпать соль";
                options[2] = "Стрижкой";
                options[3] = "Танцами с едой";
                correctOption = options[1]; // Б
            }
            else if (memeName.contains("woman_yelling_at_cat") || memeName.contains("женщина_кричит_на_кота")) {
                question = "Что делает мем \"Женщина кричит на кота\" забавным?";
                options[0] = "Игра слов";
                options[1] = "Контраст эмоций женщины и безразличного кота";
                options[2] = "Использование знаменитостей";
                options[3] = "Необычный фон";
                correctOption = options[1]; // Б
            }
            else {
                // Если не нашли подходящий вопрос, используем стандартный вопрос
                question = "Как называется этот мем?";
                String[] defaultOptions = currentMeme.getOptions();
                if (defaultOptions != null && defaultOptions.length >= 4) {
                    options = defaultOptions;
                } else {
                    Log.e("HomeFragment", "Недостаточно вариантов ответов для мема: " + memeName);
                    Toast.makeText(requireContext(), "Ошибка загрузки вариантов ответа", Toast.LENGTH_SHORT).show();
                    return;
                }
                correctOption = currentMeme.getCorrectName();
            }
            
            // Устанавливаем вопрос и варианты ответов
            tvQuestion.setText(question);
            for (int i = 0; i < optionButtons.length; i++) {
                optionButtons[i].setText(options[i]);
                optionButtons[i].setEnabled(true);
            }
            
            // Устанавливаем правильный ответ
            if (!correctOption.isEmpty()) {
                gameManager.setTemporaryCorrectAnswer(correctOption);
            }
            
            // Обновление прогресса и статистики
            tvProgress.setText(gameManager.getProgress());
            updatePlayerStats();
            
        } catch (Exception e) {
            Log.e("HomeFragment", "Ошибка при загрузке мема: " + e.getMessage(), e);
            ivMeme.setImageResource(R.drawable.meme_placeholder);
            Toast.makeText(requireContext(), "Не удалось загрузить изображение мема", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Обновляет информацию о серии правильных ответов и уровне игрока
     */
    private void updatePlayerStats() {
        // Обновляем отображение текущей серии правильных ответов
        tvStreak.setText(gameManager.getCurrentStreak() + " 🔥");
        
        // Обновляем уровень сложности
        switch (currentDifficulty) {
            case EASY:
                tvLevel.setText("Уровень: Легкий");
                break;
            case MEDIUM:
                tvLevel.setText("Уровень: Средний");
                break;
            case HARD:
                tvLevel.setText("Уровень: Сложный");
                break;
        }
    }

    @Override
    public void onClick(View v) {
        // Проверяем, что нажатие было на кнопку-вариант
        for (int i = 0; i < optionButtons.length; i++) {
            if (v.getId() == optionButtons[i].getId()) {
                // Проверяем правильность ответа
                boolean isCorrect = gameManager.checkAnswer(optionButtons[i].getText().toString());
                
                // Визуально помечаем кнопки как правильные/неправильные и блокируем их
                for (Button btn : optionButtons) {
                    btn.setEnabled(false);
                }
                
                // Выделяем выбранную кнопку
                Button selectedButton = (Button) v;
                
                // Показываем анимированное сообщение обратной связи
                showFeedback(isCorrect);
                
                // Добавляем декоративную задержку перед переходом к следующему вопросу
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Просто переходим к следующему вопросу, проверка на окончание игры
                        // будет выполнена внутри метода moveToNextQuestion
                        moveToNextQuestion();
                    }
                }, 1500);
                break;
            }
        }
    }

    /**
     * Показывает сообщение о правильности ответа
     * @param isCorrect был ли ответ правильным
     */
    private void showFeedback(boolean isCorrect) {
        // Устанавливаем текст обратной связи
        if (isCorrect) {
            tvFeedback.setText("✅ Правильно!");
        } else {
            tvFeedback.setText("❌ Неправильно!");
        }
        
        // Делаем текст видимым и применяем анимацию 
        tvFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setAlpha(0f);
        tvFeedback.animate()
            .alpha(1f)
            .setDuration(200)
            .start();
        
        // Обновляем отображение серии
        updatePlayerStats();
    }

    /**
     * Переходит к следующему вопросу или завершает игру
     */
    private void moveToNextQuestion() {
        // Скрываем элементы обратной связи и показываем новый мем
        tvFeedback.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction(() -> {
                tvFeedback.setVisibility(View.INVISIBLE);
                
                // Анимация перехода к следующему мему
                ivMeme.animate()
                    .alpha(0f)
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        // Переход к следующему мему
                        boolean hasMoreMemes = gameManager.nextMeme();
                        if (hasMoreMemes) {
                            displayCurrentMeme();
                            
                            // Анимируем появление нового мема
                            ivMeme.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(300)
                                .start();
                            
                            // Анимируем кнопки вариантов
                            animateOptionButtons();
                        } else {
                            showResults();
                        }
                    })
                    .start();
            })
            .start();
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
        
        // Активация анимации фонов
        if (startGameContainer != null) {
            startGameContainer.setPressed(true);
            startGameContainer.setPressed(false);
        }
        
        if (gameContainer != null) {
            gameContainer.setPressed(true);
            gameContainer.setPressed(false);
        }
        
        // Проверяем, была ли уже начата игра
        if (isGameStarted && savedGameManager != null) {
            // Если да, то продолжаем игру
            gameContainer.setVisibility(View.VISIBLE);
            startGameContainer.setVisibility(View.GONE);
            
            // Скрываем элементы заголовка в режиме игры
            if (tvWelcome != null) tvWelcome.setVisibility(View.GONE);
            if (tvCurrentRank != null) tvCurrentRank.setVisibility(View.GONE);
            if (ivCurrentRank != null) ivCurrentRank.setVisibility(View.GONE);
            if (ivRankBadge != null) ivRankBadge.setVisibility(View.GONE);
            if (btnPlay != null) btnPlay.setVisibility(View.GONE);
            if (btnLeaderboard != null) btnLeaderboard.setVisibility(View.GONE);
            if (btnProfile != null) btnProfile.setVisibility(View.GONE);
            
            // Восстанавливаем предыдущую игру
            gameManager = savedGameManager;
            
            // Восстанавливаем уровень сложности из сохраненных данных
            if (savedDifficulty >= 0 && savedDifficulty < MemeData.Difficulty.values().length) {
                currentDifficulty = MemeData.Difficulty.values()[savedDifficulty];
            }
            
            // Восстанавливаем категорию из сохраненных данных
            if (savedCategory >= 0 && savedCategory < MemeData.CategoryType.values().length) {
                currentCategory = MemeData.CategoryType.values()[savedCategory];
            }
        } else {
            // Если нет, показываем экран выбора сложности
            gameContainer.setVisibility(View.GONE);
            startGameContainer.setVisibility(View.VISIBLE);
            
            // Показываем элементы заголовка на начальном экране
            if (tvWelcome != null) tvWelcome.setVisibility(View.VISIBLE);
            if (tvCurrentRank != null) tvCurrentRank.setVisibility(View.VISIBLE);
            if (ivCurrentRank != null) ivCurrentRank.setVisibility(View.VISIBLE);
            if (ivRankBadge != null) ivRankBadge.setVisibility(View.VISIBLE);
            if (btnPlay != null) btnPlay.setVisibility(View.VISIBLE);
            if (btnLeaderboard != null) btnLeaderboard.setVisibility(View.VISIBLE);
            if (btnProfile != null) btnProfile.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Метод для сброса игры, чтобы начать сначала
     */
    public static void resetGameState() {
        isGameStarted = false;
        savedGameManager = null;
        savedDifficulty = -1;
        savedCategory = -1;
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

    /**
     * Загружает информацию о пользователе из SharedPreferences
     */
    private void loadUserInfo() {
        SharedPreferences prefs = requireContext().getSharedPreferences(GameManager.PREF_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Мемолог");
        int rank = prefs.getInt(GameManager.KEY_RANK, GameManager.RANK_SKUF);
        
        // Отображаем приветствие с именем пользователя
        tvWelcome.setText(getString(R.string.welcome_user, username));
        
        // Отображаем текущий ранг пользователя
        String rankName;
        int rankIcon;
        int badgeIcon;
        
        switch (rank) {
            case GameManager.RANK_ALTUSKA:
                rankName = getString(R.string.rank_altuska);
                rankIcon = R.drawable.rankaltushka;
                badgeIcon = R.drawable.ic_settings; // Продвинутые настройки для высшего ранга
                break;
            case GameManager.RANK_ZNATOK:
                rankName = getString(R.string.rank_znatok);
                rankIcon = R.drawable.rankznatok;
                badgeIcon = R.drawable.ic_leaderboard; // Знатоки в топе лидеров
                break;
            case GameManager.RANK_SKUF:
            default:
                rankName = getString(R.string.rank_skuf);
                rankIcon = R.drawable.rankskuf;
                badgeIcon = R.drawable.ic_splash_logo; // Новичкам базовый значок
                break;
        }
        
        tvCurrentRank.setText(getString(R.string.current_rank, rankName));
        ivCurrentRank.setImageResource(rankIcon);
        ivRankBadge.setImageResource(badgeIcon);
    }
    
    /**
     * Настраивает обработчики нажатий на кнопки
     */
    private void setupClickListeners(View rootView) {
        btnPlay.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_homeFragment_to_gameModeFragment);
        });
        
        btnLeaderboard.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_homeFragment_to_leaderboardFragment);
        });
        
        btnProfile.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_homeFragment_to_profileFragment);
        });
    }
    
    /**
     * Анимирует элементы при отображении главного экрана
     */
    private void animateElements() {
        CustomAnimationUtils.fadeIn(tvWelcome, 400, 0);
        CustomAnimationUtils.fadeIn(tvCurrentRank, 400, 100);
        CustomAnimationUtils.fadeIn(ivCurrentRank, 400, 150);
        CustomAnimationUtils.fadeIn(ivRankBadge, 400, 200);
        CustomAnimationUtils.fadeIn(btnPlay, 400, 300);
        CustomAnimationUtils.fadeIn(btnLeaderboard, 400, 400);
        CustomAnimationUtils.fadeIn(btnProfile, 400, 500);
        
        // Добавляем пульсирующую анимацию для кнопки играть
        CustomAnimationUtils.pulseAnimation(btnPlay, 0.95f, 1.05f, 1000, true);
    }
} 