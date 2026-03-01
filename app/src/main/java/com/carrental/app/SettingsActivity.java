package com.carrental.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial notificationsSwitch, darkModeSwitch;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Back
        if (findViewById(R.id.backButton) != null)
            findViewById(R.id.backButton).setOnClickListener(v -> finish());

        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        darkModeSwitch      = findViewById(R.id.darkModeSwitch);

        if (notificationsSwitch != null)
            notificationsSwitch.setChecked(prefs.getBoolean("notifications", true));
        if (darkModeSwitch != null)
            darkModeSwitch.setChecked(prefs.getBoolean("darkMode", false));

        // Language
        if (findViewById(R.id.changeLanguage) != null)
            findViewById(R.id.changeLanguage).setOnClickListener(v ->
                    Toast.makeText(this, "ميزة تغيير اللغة قيد التطوير", Toast.LENGTH_SHORT).show());

        // Notifications toggle
        if (notificationsSwitch != null)
            notificationsSwitch.setOnCheckedChangeListener((btn, checked) ->
                    prefs.edit().putBoolean("notifications", checked).apply());

        // Dark Mode toggle
        if (darkModeSwitch != null)
            darkModeSwitch.setOnCheckedChangeListener((btn, checked) -> {
                prefs.edit().putBoolean("darkMode", checked).apply();
                AppCompatDelegate.setDefaultNightMode(
                        checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            });

        // Logout
        if (findViewById(R.id.logoutButton) != null)
            findViewById(R.id.logoutButton).setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("تسجيل الخروج")
                .setMessage(getString(R.string.logout_confirm))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("نعم، اخرج", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}
