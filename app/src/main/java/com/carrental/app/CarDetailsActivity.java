package com.carrental.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.CarEntity;
import com.carrental.app.data.FavoriteEntity;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarDetailsActivity extends AppCompatActivity {
    private TextView carName, carPrice, carSeats, carFuelType, carTransmission, availabilityBadge;
    private TextView avgRatingText, ratingCountText;
    private RatingBar avgRatingBar;
    private ImageView carImage;
    private MaterialButton bookButton, favoriteButton, backButton, rateCarBtn;
    private AppDatabase db;
    private CarEntity car;
    private boolean isFavorite = false;
    private static final int USER_ID = 1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        carName           = findViewById(R.id.carName);
        carPrice          = findViewById(R.id.carPrice);
        carImage          = findViewById(R.id.carImage);
        carSeats          = findViewById(R.id.carSeats);
        carFuelType       = findViewById(R.id.carFuelType);
        carTransmission   = findViewById(R.id.carTransmission);
        availabilityBadge = findViewById(R.id.availabilityBadge);
        bookButton        = findViewById(R.id.bookButton);
        favoriteButton    = findViewById(R.id.favoriteButton);
        backButton        = findViewById(R.id.backButton);
        rateCarBtn        = findViewById(R.id.rateCarBtn);
        avgRatingText     = findViewById(R.id.avgRatingText);
        avgRatingBar      = findViewById(R.id.avgRatingBar);
        ratingCountText   = findViewById(R.id.ratingCountText);

        db = AppDatabase.getDatabase(getApplicationContext());

        int carId = getIntent().getIntExtra("carId", -1);
        if (carId == -1) { finish(); return; }

        if (backButton != null) backButton.setOnClickListener(v -> finish());

        // Load everything in background
        executor.execute(() -> {
            car = db.carDao().getCarById(carId);
            if (car == null) { runOnUiThread(this::finish); return; }

            // Check if user previously booked this car
            int bookingCount = db.bookingDao().hasUserBookedCar(USER_ID, carId);
            boolean hasPreviousBooking = bookingCount > 0;

            // Check favorite
            List<FavoriteEntity> favorites = db.favoriteDao().getFavoritesByUser(USER_ID);
            for (FavoriteEntity fav : favorites) {
                if (fav.carId == car.id) { isFavorite = true; break; }
            }

            // Get average rating
            float avgRating = db.ratingDao().getAverageRating(carId);
            int ratingCount = db.ratingDao().getRatingsByCar(carId).size();

            final boolean showRate = hasPreviousBooking;
            final float finalAvg = avgRating;
            final int finalCount = ratingCount;

            runOnUiThread(() -> {
                carName.setText(car.name + " · " + car.model);
                carPrice.setText(String.valueOf((int) car.pricePerDay));
                carSeats.setText(String.valueOf(car.seats > 0 ? car.seats : 5));
                carFuelType.setText(car.fuelType != null ? car.fuelType : "بنزين");
                carTransmission.setText(car.transmission != null ? car.transmission : "أوتوماتيك");
                carImage.setImageResource(CarAdapter.getCarImage(car.name));

                // Availability badge
                if (car.available) {
                    availabilityBadge.setText("متاحة ✓");
                    availabilityBadge.setBackgroundResource(R.drawable.badge_available);
                    availabilityBadge.setTextColor(0xFF0A0E1A);
                } else {
                    availabilityBadge.setText("محجوزة");
                    availabilityBadge.setBackgroundResource(R.drawable.badge_unavailable);
                    availabilityBadge.setTextColor(0xFFFFFFFF);
                }

                // Rating display
                if (avgRatingText != null && finalCount > 0) {
                    avgRatingText.setText(String.format("%.1f", finalAvg));
                    avgRatingText.setVisibility(View.VISIBLE);
                    if (avgRatingBar != null) {
                        avgRatingBar.setRating(finalAvg);
                        avgRatingBar.setVisibility(View.VISIBLE);
                    }
                    if (ratingCountText != null) {
                        ratingCountText.setText("(" + finalCount + " تقييم)");
                        ratingCountText.setVisibility(View.VISIBLE);
                    }
                } else if (avgRatingText != null) {
                    avgRatingText.setText("لا يوجد تقييم بعد");
                    avgRatingText.setVisibility(View.VISIBLE);
                }

                updateFavoriteButton();

                // Show rate button if user booked this car
                if (showRate && rateCarBtn != null) {
                    rateCarBtn.setVisibility(View.VISIBLE);
                    bookButton.setText("احجز مجدداً");
                }
            });
        });

        // Book button
        if (bookButton != null) {
            bookButton.setOnClickListener(v -> {
                if (car == null) return;
                if (car.available) {
                    startActivity(new Intent(this, BookingActivity.class)
                            .putExtra("carId", car.id));
                } else {
                    Toast.makeText(this, "هذه السيارة غير متاحة حالياً", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Rate button
        if (rateCarBtn != null) {
            rateCarBtn.setOnClickListener(v -> {
                if (car != null)
                    startActivity(new Intent(this, RatingActivity.class).putExtra("carId", car.id));
            });
        }

        // Favorite toggle
        if (favoriteButton != null) favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh rating when returning from RatingActivity
        if (car != null) {
            executor.execute(() -> {
                float avg = db.ratingDao().getAverageRating(car.id);
                int cnt   = db.ratingDao().getRatingsByCar(car.id).size();
                runOnUiThread(() -> {
                    if (avgRatingText != null && cnt > 0) {
                        avgRatingText.setText(String.format("%.1f", avg));
                        if (avgRatingBar != null) avgRatingBar.setRating(avg);
                        if (ratingCountText != null) ratingCountText.setText("(" + cnt + " تقييم)");
                    }
                });
            });
        }
    }

    private void toggleFavorite() {
        if (car == null) return;
        executor.execute(() -> {
            List<FavoriteEntity> favorites = db.favoriteDao().getFavoritesByUser(USER_ID);
            if (isFavorite) {
                for (FavoriteEntity fav : favorites) {
                    if (fav.carId == car.id) { db.favoriteDao().deleteFavorite(fav); break; }
                }
                isFavorite = false;
                runOnUiThread(() -> {
                    Toast.makeText(this, "تم الإزالة من المفضلة", Toast.LENGTH_SHORT).show();
                    updateFavoriteButton();
                });
            } else {
                FavoriteEntity fav = new FavoriteEntity();
                fav.carId = car.id; fav.userId = USER_ID;
                db.favoriteDao().insertFavorite(fav);
                isFavorite = true;
                runOnUiThread(() -> {
                    Toast.makeText(this, "تمت الإضافة للمفضلة ❤️", Toast.LENGTH_SHORT).show();
                    updateFavoriteButton();
                });
            }
        });
    }

    private void updateFavoriteButton() {
        if (favoriteButton == null) return;
        favoriteButton.setIconResource(isFavorite ?
                android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
