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
        mAuth = FirebaseAuth.getInstance();
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        
        // Проверка, что анонимная аутентификация включена
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        Log.d(TAG, "Анонимная аутентификация успешно включена");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Ошибка анонимной аутентификации: " + e.getMessage());
                    });
        }
        
        mFirestore = FirebaseFirestore.getInstance();
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
        mAuth.signInAnonymously()
            .addOnCompleteListener(anonTask -> {
                if (anonTask.isSuccessful() && anonTask.getResult() != null) {
                    FirebaseUser user = anonTask.getResult().getUser();
                    if (user != null) {
                        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                        
                        user.linkWithCredential(credential)
                            .addOnCompleteListener(linkTask -> {
                                if (linkTask.isSuccessful() && linkTask.getResult() != null) {
                                    FirebaseUser linkedUser = linkTask.getResult().getUser();
                                    
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();
                                    
                                    linkedUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            if (profileTask.isSuccessful()) {
                                                User userModel = new User(linkedUser.getUid(), username, email);
                                                mFirestore.collection("users")
                                                    .document(linkedUser.getUid())
                                                    .set(userModel)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "User profile created successfully");
                                                        listener.onSuccess(linkedUser);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Error creating user profile", e);
                                                        listener.onFailure(e.getMessage());
                                                    });
                                            } else {
                                                Log.e(TAG, "Error updating user profile", profileTask.getException());
                                                listener.onFailure("Ошибка при обновлении профиля пользователя");
                                            }
                                        });
                                } else {
                                    Log.e(TAG, "Error linking anonymous user", linkTask.getException());
                                    listener.onFailure("Ошибка при связывании анонимного пользователя с Email");
                                }
                            });
                    } else {
                        listener.onFailure("Не удалось создать анонимного пользователя");
                    }
                } else {
                    String errorMessage = anonTask.getException() != null ? 
                        anonTask.getException().getMessage() : "Неизвестная ошибка при анонимной регистрации";
                    Log.e(TAG, "Anonymous registration failed: " + errorMessage);
                    listener.onFailure(errorMessage);
                }
            });
    }

    public void registerUserOriginal(String username, String email, String password, OnAuthResultListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Отправляем письмо с подтверждением
                            sendEmailVerification(firebaseUser, success -> {
                                if (success) {
                                    // Создаем запись пользователя в Firestore
                                    User user = new User(firebaseUser.getUid(), username, email);
                                    mFirestore.collection("users")
                                            .document(firebaseUser.getUid())
                                            .set(user)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "User profile created successfully");
                                                listener.onSuccess(firebaseUser);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error creating user profile", e);
                                                // Если не удалось создать профиль, удаляем пользователя из аутентификации
                                                firebaseUser.delete();
                                                listener.onFailure(e.getMessage());
                                            });
                                } else {
                                    listener.onFailure("Не удалось отправить письмо для подтверждения");
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