package com.carrental.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNav(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;

            Intent intent = null;
            int flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

            if (id == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP;
            } else if (id == R.id.nav_bookings) {
                intent = new Intent(this, MyBookingsActivity.class);
            } else if (id == R.id.nav_favorites) {
                intent = new Intent(this, FavoritesActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(flags);
                startActivity(intent);
                overridePendingTransition(0, 0); // No animation = native feel
            }
            return true;
        });
    }
}
