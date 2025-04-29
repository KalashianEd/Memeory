package com.kalashianed.memeory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.kalashianed.memeory.auth.AuthManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView, forgotPasswordTextView;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация AuthManager
        authManager = AuthManager.getInstance(this);

        // Если пользователь уже вошел и верифицирован, перенаправляем на главный экран
        if (authManager.isUserLoggedIn() && authManager.isEmailVerified()) {
            startMainActivity();
            finish();
            return;
        }

        // Инициализация UI элементов
        emailLayout = findViewById(R.id.emailLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        progressBar = findViewById(R.id.progressBar);

        // Обработчик нажатия на кнопку входа
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Валидация полей
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Введите email");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Введите пароль");
                return;
            }

            // Показываем индикатор загрузки
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);

            // Выполняем вход
            authManager.loginUser(email, password, new AuthManager.OnAuthResultListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    
                    // Проверяем, связана ли ошибка с неподтвержденным email
                    if (errorMessage.contains("Email не подтвержден")) {
                        showEmailVerificationDialog();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        // Обработчик нажатия на текст регистрации
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // Обработчик нажатия на "Забыли пароль?"
        forgotPasswordTextView.setOnClickListener(v -> showForgotPasswordDialog());
    }
    
    /**
     * Показывает диалог для отправки письма с подтверждением email
     */
    private void showEmailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email не подтвержден")
               .setMessage("Для входа в приложение необходимо подтвердить email. Проверьте свою почту или отправьте письмо повторно.")
               .setPositiveButton("Отправить повторно", (dialog, which) -> {
                   // Получаем текущего пользователя и отправляем письмо с подтверждением
                   FirebaseUser user = authManager.getCurrentUser();
                   if (user != null) {
                       progressBar.setVisibility(View.VISIBLE);
                       authManager.sendEmailVerification(user, success -> {
                           progressBar.setVisibility(View.GONE);
                           if (success) {
                               Toast.makeText(LoginActivity.this, 
                                       "Письмо с подтверждением отправлено на " + user.getEmail(), 
                                       Toast.LENGTH_LONG).show();
                           } else {
                               Toast.makeText(LoginActivity.this, 
                                       "Не удалось отправить письмо с подтверждением", 
                                       Toast.LENGTH_LONG).show();
                           }
                       });
                   }
               })
               .setNegativeButton("Отмена", null)
               .show();
    }
    
    /**
     * Показывает диалог для отправки письма со сбросом пароля
     */
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        final TextInputEditText resetEmailEditText = view.findViewById(R.id.resetEmailEditText);
        
        builder.setTitle("Сброс пароля")
               .setView(view)
               .setPositiveButton("Отправить", (dialog, which) -> {
                   String email = resetEmailEditText.getText().toString().trim();
                   if (!TextUtils.isEmpty(email)) {
                       progressBar.setVisibility(View.VISIBLE);
                       authManager.resetPassword(email, success -> {
                           progressBar.setVisibility(View.GONE);
                           if (success) {
                               Toast.makeText(LoginActivity.this, 
                                       "Инструкции по сбросу пароля отправлены на " + email, 
                                       Toast.LENGTH_LONG).show();
                           } else {
                               Toast.makeText(LoginActivity.this, 
                                       "Не удалось отправить письмо для сброса пароля", 
                                       Toast.LENGTH_LONG).show();
                           }
                       });
                   } else {
                       Toast.makeText(LoginActivity.this, "Введите email", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("Отмена", null)
               .show();
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 