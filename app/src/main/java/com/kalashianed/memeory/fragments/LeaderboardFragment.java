package com.kalashianed.memeory.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.kalashianed.memeory.R;
import com.kalashianed.memeory.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";
    
    private RecyclerView rvLeaderboard;
    private TextView tvFirstPlaceName, tvFirstPlaceScore;
    private TextView tvSecondPlaceName, tvSecondPlaceScore;
    private TextView tvThirdPlaceName, tvThirdPlaceScore;
    private TextView tvNoData;
    private ProgressBar progressLoading;
    
    private Button btnFilterGlobal;
    
    private LeaderboardAdapter adapter;
    private List<User> usersList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        
        // Инициализация Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Инициализация UI элементов
        rvLeaderboard = root.findViewById(R.id.rvLeaderboard);
        tvNoData = root.findViewById(R.id.tvNoData);
        progressLoading = root.findViewById(R.id.progressLoading);
        
        // Инициализация элементов топ-3
        tvFirstPlaceName = root.findViewById(R.id.tvFirstPlaceName);
        tvFirstPlaceScore = root.findViewById(R.id.tvFirstPlaceScore);
        tvSecondPlaceName = root.findViewById(R.id.tvSecondPlaceName);
        tvSecondPlaceScore = root.findViewById(R.id.tvSecondPlaceScore);
        tvThirdPlaceName = root.findViewById(R.id.tvThirdPlaceName);
        tvThirdPlaceScore = root.findViewById(R.id.tvThirdPlaceScore);
        
        // Инициализация кнопки фильтра
        btnFilterGlobal = root.findViewById(R.id.btnFilterGlobal);
        
        // Показываем индикатор загрузки
        progressLoading.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);
        
        // Настройка RecyclerView
        adapter = new LeaderboardAdapter(getContext(), usersList, currentUser != null ? currentUser.getUid() : null);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeaderboard.setAdapter(adapter);
        
        // Загрузка данных лидерборда
        loadLeaderboardData();
        
        // Настройка кнопки для обновления данных при нажатии
        btnFilterGlobal.setOnClickListener(v -> {
            progressLoading.setVisibility(View.VISIBLE);
            rvLeaderboard.setVisibility(View.GONE);
            tvNoData.setVisibility(View.GONE);
            loadLeaderboardData();
        });
        
        // Если мы авторизованы, но данных нет, создаем тестовых пользователей для демонстрации
        if (currentUser != null) {
            tvNoData.setOnClickListener(v -> {
                createTestUsers();
            });
        }
        
        return root;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновляем данные при возвращении к фрагменту
        loadLeaderboardData();
    }
    
    /**
     * Создает тестовых пользователей, если в базе нет данных
     */
    private void createTestUsers() {
        if (currentUser == null) return;
        
        progressLoading.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        
        // Создаем 10 тестовых пользователей для демонстрации
        List<Map<String, Object>> testUsers = new ArrayList<>();
        
        // Текущий пользователь
        Map<String, Object> currentUserData = new HashMap<>();
        currentUserData.put("userId", currentUser.getUid());
        currentUserData.put("username", currentUser.getDisplayName() != null ? 
                currentUser.getDisplayName() : "Вы");
        currentUserData.put("email", currentUser.getEmail());
        currentUserData.put("bestScore", 75); // Ставим средний результат
        currentUserData.put("bestStreak", 3);
        
        // Сохраняем текущего пользователя
        firestore.collection("users").document(currentUser.getUid())
                .set(currentUserData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Данные текущего пользователя добавлены");
                    
                    // Создаем несколько тестовых пользователей
                    for (int i = 1; i <= 10; i++) {
                        String userId = "test_user_" + i;
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userId", userId);
                        userData.put("username", "Игрок " + i);
                        userData.put("email", "user" + i + "@example.com");
                        
                        // Распределяем рейтинг, чтобы создать реалистичную таблицу
                        int score;
                        if (i == 1) score = 95; // Лидер
                        else if (i == 2) score = 90; // Второе место
                        else if (i == 3) score = 85; // Третье место
                        else score = 70 - (i * 5); // Остальные с убывающим рейтингом
                        
                        userData.put("bestScore", score);
                        userData.put("bestStreak", 10 - i);
                        
                        // Сохраняем пользователя
                        firestore.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "Тестовый пользователь добавлен");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Ошибка при добавлении тестового пользователя", e);
                                });
                    }
                    
                    // Перезагружаем данные через 2 секунды (чтобы успели записаться)
                    new android.os.Handler().postDelayed(() -> {
                        loadLeaderboardData();
                    }, 2000);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка при добавлении текущего пользователя", e);
                    progressLoading.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.VISIBLE);
                });
    }
    
    /**
     * Загружает данные для лидерборда из Firestore
     */
    private void loadLeaderboardData() {
        // Очищаем текущий список
        usersList.clear();
        
        // Показываем индикатор загрузки
        progressLoading.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);
        
        Log.d(TAG, "Начало загрузки данных лидерборда");
        
        // Запрос для получения пользователей, отсортированных по лучшему счету (нисходящий порядок)
        // Увеличиваем лимит до 100 пользователей, чтобы отобразить больше участников
        firestore.collection("users")
                .orderBy("bestScore", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> topUsers = new ArrayList<>();
                    
                    Log.d(TAG, "Получены данные: " + queryDocumentSnapshots.size() + " пользователей");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        
                        // Убедимся, что у пользователя установлен ID
                        if (user.getUserId() == null) {
                            user.setUserId(document.getId());
                        }
                        
                        // Убедимся, что у пользователя есть имя
                        if (user.getUsername() == null || user.getUsername().isEmpty()) {
                            user.setUsername("Пользователь " + document.getId().substring(0, Math.min(5, document.getId().length())));
                        }
                        
                        // Проверяем, что у пользователя есть хотя бы какой-то рейтинг
                        if (user.getBestScore() <= 0) {
                            user.setBestScore(10); // Устанавливаем минимальный рейтинг
                        }
                        
                        topUsers.add(user);
                        Log.d(TAG, "Пользователь: " + user.getUsername() + ", Счет: " + user.getBestScore());
                    }
                    
                    // Скрываем индикатор загрузки
                    progressLoading.setVisibility(View.GONE);
                    
                    if (topUsers.isEmpty()) {
                        // Если данных нет, показываем сообщение
                        tvNoData.setText("Нет пользователей в рейтинге. Нажмите здесь, чтобы создать тестовые данные.");
                        tvNoData.setVisibility(View.VISIBLE);
                        rvLeaderboard.setVisibility(View.GONE);
                        Log.d(TAG, "Нет данных о пользователях");
                        return;
                    }
                    
                    // Обновляем UI с топ-3
                    updateTopThreeUI(topUsers);
                    
                    // Если у нас есть только топ-3 (или меньше), показываем сообщение о том, что нет других пользователей
                    if (topUsers.size() <= 3) {
                        tvNoData.setText("Всего " + topUsers.size() + " пользователей в рейтинге");
                        tvNoData.setVisibility(View.VISIBLE);
                        // Показываем всех пользователей (даже если их меньше 3)
                        usersList.addAll(topUsers);
                        adapter.notifyDataSetChanged();
                        rvLeaderboard.setVisibility(View.VISIBLE);
                    } else {
                        // Убираем первые 3 пользователя (они отображаются отдельно)
                        usersList.addAll(topUsers.subList(Math.min(3, topUsers.size()), topUsers.size()));
                        rvLeaderboard.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                        
                        Log.d(TAG, "Отображение " + usersList.size() + " пользователей в списке 'Остальные участники'");
                    }
                    
                    // Уведомляем адаптер об изменениях
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Скрываем индикатор загрузки
                    progressLoading.setVisibility(View.GONE);
                    tvNoData.setText("Ошибка загрузки данных: " + e.getMessage());
                    tvNoData.setVisibility(View.VISIBLE);
                    rvLeaderboard.setVisibility(View.GONE);
                    
                    Log.e(TAG, "Ошибка загрузки лидерборда: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Ошибка загрузки лидерборда: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    /**
     * Обновляет UI с топ-3 игроками
     */
    private void updateTopThreeUI(List<User> users) {
        if (users.size() > 0) {
            User firstPlace = users.get(0);
            tvFirstPlaceName.setText(firstPlace.getUsername());
            tvFirstPlaceScore.setText(String.format("%d pts", firstPlace.getBestScore()));
            Log.d(TAG, "Первое место: " + firstPlace.getUsername() + " (" + firstPlace.getBestScore() + ")");
        } else {
            tvFirstPlaceName.setText("—");
            tvFirstPlaceScore.setText("0 pts");
        }
        
        if (users.size() > 1) {
            User secondPlace = users.get(1);
            tvSecondPlaceName.setText(secondPlace.getUsername());
            tvSecondPlaceScore.setText(String.format("%d pts", secondPlace.getBestScore()));
            Log.d(TAG, "Второе место: " + secondPlace.getUsername() + " (" + secondPlace.getBestScore() + ")");
        } else {
            tvSecondPlaceName.setText("—");
            tvSecondPlaceScore.setText("0 pts");
        }
        
        if (users.size() > 2) {
            User thirdPlace = users.get(2);
            tvThirdPlaceName.setText(thirdPlace.getUsername());
            tvThirdPlaceScore.setText(String.format("%d pts", thirdPlace.getBestScore()));
            Log.d(TAG, "Третье место: " + thirdPlace.getUsername() + " (" + thirdPlace.getBestScore() + ")");
        } else {
            tvThirdPlaceName.setText("—");
            tvThirdPlaceScore.setText("0 pts");
        }
    }
    
    /**
     * Адаптер для отображения списка лидеров
     */
    private static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
        
        private Context context;
        private List<User> users;
        private String currentUserId;
        private boolean isShowingAllUsers;
        
        public LeaderboardAdapter(Context context, List<User> users, String currentUserId) {
            this.context = context;
            this.users = users;
            this.currentUserId = currentUserId;
            // Определяем, отображаем ли мы всех пользователей, включая топ-3
            this.isShowingAllUsers = users.size() <= 3;
        }
        
        @NonNull
        @Override
        public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
            return new LeaderboardViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
            User user = users.get(position);
            
            // Устанавливаем ранг в зависимости от того, показываем ли мы всех пользователей или только дополнительных
            int rankPosition = isShowingAllUsers ? position + 1 : position + 4;
            holder.tvRank.setText(String.valueOf(rankPosition));
            
            // Устанавливаем имя пользователя
            holder.tvPlayerName.setText(user.getUsername());
            
            // Устанавливаем счет
            holder.tvScore.setText(String.format("%d pts", user.getBestScore()));
            
            // Выделение текущего пользователя
            if (currentUserId != null && currentUserId.equals(user.getUserId())) {
                // Используем цвет с альфа-каналом для выделения текущего пользователя
                holder.itemView.setBackgroundResource(R.drawable.current_user_leaderboard_background);
            } else {
                // Используем прозрачный цвет для остальных пользователей
                holder.itemView.setBackgroundResource(android.R.color.transparent);
            }
            
            // Устанавливаем разный цвет фона для ранга в зависимости от позиции
            if (rankPosition == 1) {
                holder.tvRank.setBackgroundResource(R.drawable.rank_gold_background);
            } else if (rankPosition == 2) {
                holder.tvRank.setBackgroundResource(R.drawable.rank_silver_background);
            } else if (rankPosition == 3) {
                holder.tvRank.setBackgroundResource(R.drawable.rank_bronze_background);
            } else {
                holder.tvRank.setBackgroundResource(R.drawable.circle_rank_background);
            }
        }
        
        @Override
        public int getItemCount() {
            return users.size();
        }
        
        static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvPlayerName, tvScore;
            ImageView ivPlayerAvatar;
            
            LeaderboardViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRank = itemView.findViewById(R.id.tvRank);
                tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
                tvScore = itemView.findViewById(R.id.tvScore);
                ivPlayerAvatar = itemView.findViewById(R.id.ivPlayerAvatar);
            }
        }
    }
} 