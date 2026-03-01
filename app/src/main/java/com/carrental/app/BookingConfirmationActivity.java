package com.carrental.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class BookingConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        TextView  confirmCarName     = findViewById(R.id.confirmCarName);
        TextView  confirmDuration    = findViewById(R.id.confirmBookingDuration);
        TextView  confirmPrice       = findViewById(R.id.confirmTotalPrice);
        ImageView confirmCarImage    = findViewById(R.id.confirmCarImage);
        MaterialButton goHomeBtn     = findViewById(R.id.finalConfirmBtn);
        MaterialButton rateNowBtn    = findViewById(R.id.rateNowBtn);

        String carName  = getIntent().getStringExtra("carName");
        String duration = getIntent().getStringExtra("bookingDuration");
        int    price    = getIntent().getIntExtra("totalPrice", 0);
        int    carId    = getIntent().getIntExtra("carId", -1);

        if (carName != null) {
            if (confirmCarName  != null) confirmCarName.setText(carName);
            if (confirmCarImage != null) confirmCarImage.setImageResource(CarAdapter.getCarImage(carName));
        }
        if (duration != null && confirmDuration != null)
            confirmDuration.setText("المدة: " + duration);
        if (confirmPrice != null)
            confirmPrice.setText(price + " $");

        // Go Home - clear booking stack
        if (goHomeBtn != null) {
            goHomeBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // Rate now
        if (rateNowBtn != null) {
            if (carId != -1) {
                rateNowBtn.setVisibility(View.VISIBLE);
                rateNowBtn.setOnClickListener(v -> {
                    startActivity(new Intent(this, RatingActivity.class).putExtra("carId", carId));
                });
            } else {
                rateNowBtn.setVisibility(View.GONE);
            }
        }

        // No back button on confirmation — user must go Home or Rate
        // Disable back press to prevent going back to booking
    }

    @Override
    public void onBackPressed() {
        // Navigate to home instead of back to booking
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
