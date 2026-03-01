package com.carrental.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.UserEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends BaseActivity {

    private TextInputEditText userName, userEmail, userPhone;
    private TextView userDisplayName, userDisplayEmail;
    private MaterialButton saveButton;
    private AppDatabase db;
    private int userId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Back
        if (findViewById(R.id.backButton) != null)
            findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Settings icon in header
        if (findViewById(R.id.settingsBtn) != null)
            findViewById(R.id.settingsBtn).setOnClickListener(v ->
                    startActivity(new Intent(this, SettingsActivity.class)));

        // Quick links cards
        if (findViewById(R.id.settingsCard) != null)
            findViewById(R.id.settingsCard).setOnClickListener(v ->
                    startActivity(new Intent(this, SettingsActivity.class)));

        if (findViewById(R.id.myRatingsCard) != null)
            findViewById(R.id.myRatingsCard).setOnClickListener(v ->
                    startActivity(new Intent(this, MyBookingsActivity.class)));

        userName         = findViewById(R.id.userName);
        userEmail        = findViewById(R.id.userEmail);
        userPhone        = findViewById(R.id.userPhone);
        saveButton       = findViewById(R.id.saveButton);
        userDisplayName  = findViewById(R.id.userDisplayName);
        userDisplayEmail = findViewById(R.id.userDisplayEmail);

        db = AppDatabase.getDatabase(getApplicationContext());

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        String storedName  = prefs.getString("userName",  "");
        String storedEmail = prefs.getString("userEmail", "");

        // Show cached prefs immediately for snappy feel
        if (userDisplayName  != null) userDisplayName.setText(storedName.isEmpty() ? "المستخدم" : storedName);
        if (userDisplayEmail != null) userDisplayEmail.setText(storedEmail);
        if (userName  != null) userName.setText(storedName);
        if (userEmail != null) userEmail.setText(storedEmail);

        // Then load fresh from DB
        if (userId != -1) {
            executor.execute(() -> {
                UserEntity user = db.userDao().getUserById(userId);
                if (user != null) {
                    runOnUiThread(() -> {
                        if (userName         != null) userName.setText(user.name);
                        if (userEmail        != null) userEmail.setText(user.email);
                        if (userPhone        != null) userPhone.setText(user.phone);
                        if (userDisplayName  != null) userDisplayName.setText(user.name);
                        if (userDisplayEmail != null) userDisplayEmail.setText(user.email);
                    });
                }
            });
        }

        if (saveButton != null) saveButton.setOnClickListener(v -> saveProfile());
        setupBottomNav(R.id.nav_profile);
    }

    private void saveProfile() {
        String name  = userName  != null && userName.getText()  != null ? userName.getText().toString().trim()  : "";
        String email = userEmail != null && userEmail.getText() != null ? userEmail.getText().toString().trim() : "";
        String phone = userPhone != null && userPhone.getText() != null ? userPhone.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "يرجى إدخال الاسم", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "صيغة البريد غير صحيحة", Toast.LENGTH_SHORT).show();
            return;
        }

        if (saveButton != null) saveButton.setEnabled(false);

        executor.execute(() -> {
            if (userId != -1) {
                UserEntity user = db.userDao().getUserById(userId);
                if (user != null) {
                    user.name = name; user.email = email; user.phone = phone;
                    db.userDao().updateUser(user);
                }
            }
            getSharedPreferences("settings", MODE_PRIVATE).edit()
                    .putString("userName",  name)
                    .putString("userEmail", email)
                    .apply();
            runOnUiThread(() -> {
                if (saveButton       != null) saveButton.setEnabled(true);
                if (userDisplayName  != null) userDisplayName.setText(name);
                if (userDisplayEmail != null) userDisplayEmail.setText(email);
                Toast.makeText(this, getString(R.string.changes_saved), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
