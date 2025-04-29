package com.kalashianed.memeory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.kalashianed.memeory.auth.AuthManager;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация AuthManager
        authManager = AuthManager.getInstance(this);

        // Инициализация UI элементов
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        progressBar = findViewById(R.id.progressBar);

        // Обработчик нажатия на кнопку регистрации
        registerButton.setOnClickListener(v -> {
            // Получение данных из полей ввода
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            
            // Проверка валидности данных
            if (TextUtils.isEmpty(name)) {
                nameEditText.setError("Введите имя");
                return;
            }
            
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Введите email");
                return;
            }
            
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Введите пароль");
                return;
            }
            
            if (password.length() < 6) {
                passwordEditText.setError("Пароль должен содержать не менее 6 символов");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Пароли не совпадают");
                return;
            }
            
            // Показываем индикатор загрузки
            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
            
            // Выводим дополнительную информацию для отладки
            Log.d("RegisterActivity", "Attempting to register user with email: " + email);
            
            // Регистрация пользователя
            authManager.registerUser(name, email, password, new AuthManager.OnAuthResultListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    
                    Log.d("RegisterActivity", "Registration successful for user: " + user.getUid());
                    
                    // Показываем диалог с информацией о верификации email
                    showEmailVerificationDialog(email);
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    
                    Log.e("RegisterActivity", "Registration failed: " + errorMessage);
                    
                    // Показываем более подробное сообщение об ошибке
                    Toast.makeText(RegisterActivity.this, "Ошибка регистрации: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        // Обработчик нажатия на текст входа
        loginTextView.setOnClickListener(v -> {
            finish(); // Возвращаемся на экран входа
        });
    }
    
    /**
     * Показывает диалог с информацией о необходимости подтверждения email
     */
    private void showEmailVerificationDialog(String email) {
        new AlertDialog.Builder(this)
            .setTitle("Подтвердите ваш email")
            .setMessage("Регистрация успешна! На адрес " + email + " отправлено письмо со ссылкой для подтверждения. " +
                    "Подтвердите ваш email, чтобы войти в приложение.")
            .setPositiveButton("Ок", (dialog, which) -> {
                // Переходим на экран входа
                finish();
            })
            .setCancelable(false) // Запрещаем закрытие диалога кнопкой "Назад"
            .show();
    }

    private void startMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 