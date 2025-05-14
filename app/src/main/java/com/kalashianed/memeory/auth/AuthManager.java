package com.kalashianed.memeory.auth;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kalashianed.memeory.model.User;

/**
 * Класс для управления процессами аутентификации Firebase.
 */
public class AuthManager {
    private static final String TAG = "AuthManager";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static AuthManager instance;

    public AuthManager(Context context) {
        try {
            mAuth = FirebaseAuth.getInstance();
            
            // Отключаем проверку телефона только для тестирования
            // Это может быть причиной ошибки безопасности
            // mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
            
            mFirestore = FirebaseFirestore.getInstance();
            Log.d(TAG, "AuthManager успешно инициализирован");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при инициализации AuthManager: " + e.getMessage(), e);
            // Повторная попытка инициализации в случае ошибки
            try {
                mAuth = FirebaseAuth.getInstance();
                mFirestore = FirebaseFirestore.getInstance();
                Log.d(TAG, "AuthManager инициализирован со второй попытки");
            } catch (Exception ex) {
                Log.e(TAG, "Критическая ошибка при инициализации AuthManager: " + ex.getMessage(), ex);
            }
        }
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
    
    public boolean isEmailVerified() {
        FirebaseUser user = getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public void registerUser(String username, String email, String password, OnAuthResultListener listener) {
        // Используем прямой метод создания пользователя без анонимной авторизации
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Отправляем письмо для верификации email
                            firebaseUser.sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (!verificationTask.isSuccessful()) {
                                        Log.e(TAG, "Failed to send verification email", verificationTask.getException());
                                    } else {
                                        Log.d(TAG, "Verification email sent");
                                    }
                                });
                                
                            // Сразу сообщаем об успехе после создания пользователя и отправки письма
                            // чтобы не блокировать UI ожиданием завершения других операций
                            listener.onSuccess(firebaseUser);
                                
                            // Устанавливаем имя пользователя после отправки успешного результата
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            
                            firebaseUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        // Создаем запись пользователя в Firestore
                                        User user = new User(firebaseUser.getUid(), username, email);
                                        mFirestore.collection("users")
                                                .document(firebaseUser.getUid())
                                                .set(user)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "User profile created successfully");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error creating user profile", e);
                                                });
                                    } else {
                                        Log.e(TAG, "Error updating user profile", profileTask.getException());
                                    }
                                });
                        } else {
                            listener.onFailure("Failed to create user");
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error occurred";
                        Log.e(TAG, "Registration failed: " + errorMessage);
                        listener.onFailure(errorMessage);
                    }
                });
    }

    /**
     * Отправляет письмо с подтверждением адреса электронной почты.
     */
    public void sendEmailVerification(FirebaseUser user, OnVerificationSentListener listener) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Verification email sent");
                        listener.onVerificationSent(true);
                    } else {
                        Log.e(TAG, "Failed to send verification email", task.getException());
                        listener.onVerificationSent(false);
                    }
                });
    }

    /**
     * Обновляет токен пользователя для проверки статуса верификации электронной почты.
     */
    public void reloadUser(OnReloadUserListener listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onUserReloaded(true);
                        } else {
                            listener.onUserReloaded(false);
                        }
                    });
        } else {
            listener.onUserReloaded(false);
        }
    }

    public void loginUser(String email, String password, OnAuthResultListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Временно отключаем проверку подтверждения email
                            listener.onSuccess(firebaseUser);
                            /* Оригинальная проверка на подтверждение email
                            if (firebaseUser.isEmailVerified()) {
                                listener.onSuccess(firebaseUser);
                            } else {
                                // Если email не подтвержден, предлагаем отправить письмо заново
                                listener.onFailure("Email не подтвержден. Пожалуйста, проверьте вашу почту или запросите новое письмо для подтверждения.");
                            }
                            */
                        } else {
                            listener.onFailure("Failed to login");
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error occurred";
                        Log.e(TAG, "Login failed: " + errorMessage);
                        listener.onFailure(errorMessage);
                    }
                });
    }

    /**
     * Отправка письма для сброса пароля.
     */
    public void resetPassword(String email, OnPasswordResetListener listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onPasswordResetEmailSent(true);
                    } else {
                        listener.onPasswordResetEmailSent(false);
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }

    public void addAuthStateListener(FirebaseAuth.AuthStateListener authStateListener) {
        this.mAuthListener = authStateListener;
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void removeAuthStateListener() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
            mAuthListener = null;
        }
    }

    public interface OnAuthResultListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }
    
    public interface OnVerificationSentListener {
        void onVerificationSent(boolean success);
    }
    
    public interface OnReloadUserListener {
        void onUserReloaded(boolean success);
    }
    
    public interface OnPasswordResetListener {
        void onPasswordResetEmailSent(boolean success);
    }
} 