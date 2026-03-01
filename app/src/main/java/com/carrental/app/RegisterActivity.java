package com.carrental.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.UserEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText name, email, password, phone;
    private MaterialButton registerBtn;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name       = findViewById(R.id.name);
        email      = findViewById(R.id.email);
        password   = findViewById(R.id.password);
        phone      = findViewById(R.id.phone);
        registerBtn = findViewById(R.id.registerBtn);

        if (findViewById(R.id.backBtn) != null)
            findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        db = AppDatabase.getDatabase(getApplicationContext());

        registerBtn.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String userName  = name.getText()     != null ? name.getText().toString().trim()     : "";
        String userEmail = email.getText()    != null ? email.getText().toString().trim()     : "";
        String userPass  = password.getText() != null ? password.getText().toString().trim()  : "";
        String userPhone = phone.getText()    != null ? phone.getText().toString().trim()     : "";

        if (userName.isEmpty() || userEmail.isEmpty() || userPass.isEmpty() || userPhone.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, "صيغة البريد الإلكتروني غير صحيحة", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userPass.length() < 6) {
            Toast.makeText(this, "كلمة المرور يجب أن تكون 6 أحرف على الأقل", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userPhone.length() < 9) {
            Toast.makeText(this, "رقم الهاتف غير صحيح", Toast.LENGTH_SHORT).show();
            return;
        }

        registerBtn.setEnabled(false);

        executor.execute(() -> {
            // Check duplicate email
            int count = db.userDao().countByEmail(userEmail);
            runOnUiThread(() -> {
                if (count > 0) {
                    registerBtn.setEnabled(true);
                    Toast.makeText(this, "هذا البريد مسجّل مسبقاً، جرّب تسجيل الدخول", Toast.LENGTH_LONG).show();
                    return;
                }
                executor.execute(() -> {
                    UserEntity user = new UserEntity();
                    user.name     = userName;
                    user.email    = userEmail;
                    user.password = userPass;
                    user.phone    = userPhone;
                    db.userDao().insertUser(user);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "تم إنشاء الحساب بنجاح! سجّل دخولك الآن", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                });
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
