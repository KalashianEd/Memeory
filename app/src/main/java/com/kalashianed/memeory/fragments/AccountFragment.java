package com.kalashianed.memeory.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kalashianed.memeory.LoginActivity;
import com.kalashianed.memeory.R;
import com.kalashianed.memeory.auth.AuthManager;
import com.kalashianed.memeory.model.User;

public class AccountFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvTotalGames, tvBestScore, tvBestStreak, tvUserRank;
    private ImageView ivProfileAvatar;
    private Button btnEditProfile, btnLogout;
    
    private AuthManager authManager;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);
        
        // Инициализация Firebase
        authManager = AuthManager.getInstance(requireContext());
        firestore = FirebaseFirestore.getInstance();
        
        // Инициализация UI элементов
        tvUserName = root.findViewById(R.id.tvUserName);
        tvUserEmail = root.findViewById(R.id.tvUserEmail);
        tvTotalGames = root.findViewById(R.id.tvTotalGames);
        tvBestScore = root.findViewById(R.id.tvBestScore);
        tvBestStreak = root.findViewById(R.id.tvBestStreak);
        tvUserRank = root.findViewById(R.id.tvUserRank);
        ivProfileAvatar = root.findViewById(R.id.ivProfileAvatar);
        btnEditProfile = root.findViewById(R.id.btnEditProfile);
        btnLogout = root.findViewById(R.id.btnLogout);
        
        // Настройка кнопки выхода
        btnLogout.setOnClickListener(v -> {
            authManager.signOut();
            navigateToLogin();
        });
        
        // Настройка кнопки редактирования профиля
        btnEditProfile.setOnClickListener(v -> {
            // TODO: Добавить редактирование профиля в будущем
            Toast.makeText(requireContext(), "Эта функция будет доступна в будущих версиях", Toast.LENGTH_SHORT).show();
        });
        
        // Загрузка данных пользователя
        loadUserData();
        
        return root;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновляем данные пользователя при возвращении к фрагменту
        loadUserData();
    }
    
    /**
     * Загружает данные текущего пользователя из Firebase
     */
    private void loadUserData() {
        FirebaseUser firebaseUser = authManager.getCurrentUser();
        if (firebaseUser == null) {
            navigateToLogin();
            return;
        }
        
        // Установка базовой информации из Firebase Auth
        tvUserName.setText(firebaseUser.getDisplayName());
        tvUserEmail.setText(firebaseUser.getEmail());
        
        // Получение дополнительных данных из Firestore
        firestore.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            updateUIWithUserData(user);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        
        // Получение ранга пользователя
        getUserRank(firebaseUser.getUid());
    }
    
    /**
     * Обновляет UI с данными пользователя
     */
    private void updateUIWithUserData(User user) {
        // Обновляем имя пользователя (если есть в Firestore)
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            tvUserName.setText(user.getUsername());
        }
        
        // Устанавливаем статистику
        tvBestScore.setText(String.format("%d%%", user.getBestScore()));
        tvBestStreak.setText(String.format("%d 🔥", user.getBestStreak()));
        
        // Получаем количество сыгранных игр (добавим в будущем)
        firestore.collection("users").document(user.getUserId())
                .collection("games")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int gamesCount = queryDocumentSnapshots.size();
                    tvTotalGames.setText(String.valueOf(gamesCount));
                })
                .addOnFailureListener(e -> {
                    tvTotalGames.setText("0");
                });
    }
    
    /**
     * Получает ранг пользователя в общем рейтинге
     */
    private void getUserRank(String userId) {
        firestore.collection("users")
                .orderBy("bestScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int rank = 0;
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        rank++;
                        if (documentSnapshot.getId().equals(userId)) {
                            break;
                        }
                    }
                    tvUserRank.setText(String.valueOf(rank));
                })
                .addOnFailureListener(e -> {
                    tvUserRank.setText("-");
                    Toast.makeText(requireContext(), "Ошибка получения рейтинга", Toast.LENGTH_SHORT).show();
                });
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