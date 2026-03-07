package com.carrental.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView logoIcon = findViewById(R.id.logoIcon);
        TextView appName  = findViewById(R.id.appName);

        if (logoIcon != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(600);
            logoIcon.startAnimation(fadeIn);
        }
        if (appName != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(800);
            fadeIn.setStartOffset(200);
            appName.startAnimation(fadeIn);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;

            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            boolean loggedIn      = prefs.getBoolean("loggedIn", false);
            boolean onboardingDone = prefs.getBoolean("onboardingDone", false);

            Intent intent;
            if (loggedIn) {
                intent = new Intent(this, HomeActivity.class);
            } else if (!onboardingDone) {
                intent = new Intent(this, OnboardingActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000);
    }
}
