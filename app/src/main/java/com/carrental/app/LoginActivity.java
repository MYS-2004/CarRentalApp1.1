package com.carrental.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.UserEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton, registerButton;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput   = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton   = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        db = AppDatabase.getDatabase(getApplicationContext());

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "صيغة البريد الإلكتروني غير صحيحة", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        executor.execute(() -> {
            UserEntity user = db.userDao().login(email, password);
            runOnUiThread(() -> {
                loginButton.setEnabled(true);
                if (user != null) {
                    getSharedPreferences("settings", MODE_PRIVATE).edit()
                            .putBoolean("loggedIn", true)
                            .putInt("userId", user.id)
                            .putString("userName", user.name)
                            .putString("userEmail", user.email)
                            .apply();
                    Toast.makeText(this, "أهلاً بك، " + user.name + "!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "البريد أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show();
                    passwordInput.setText("");
                    passwordInput.requestFocus();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
