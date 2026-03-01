package com.carrental.app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.CarEntity;
import com.carrental.app.data.RatingEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextInputEditText commentInput;
    private TextView ratingLabel, ratingCarName;
    private ImageView ratingCarImage;
    private MaterialButton submitBtn;
    private AppDatabase db;
    private CarEntity car;
    private int carId = -1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String[] LABELS = {
        "اختر تقييمك", "سيء 😞", "مقبول 😐", "جيد 🙂", "ممتاز 😊", "رائع جداً ⭐"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ratingBar     = findViewById(R.id.ratingBar);
        commentInput  = findViewById(R.id.commentInput);
        ratingLabel   = findViewById(R.id.ratingLabel);
        ratingCarName = findViewById(R.id.ratingCarName);
        ratingCarImage = findViewById(R.id.ratingCarImage);
        submitBtn     = findViewById(R.id.submitRatingBtn);

        db = AppDatabase.getDatabase(getApplicationContext());
        carId = getIntent().getIntExtra("carId", -1);

        if (carId == -1) { finish(); return; }

        if (findViewById(R.id.backButton) != null)
            findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Load car
        executor.execute(() -> {
            car = db.carDao().getCarById(carId);
            if (car == null) { runOnUiThread(this::finish); return; }
            runOnUiThread(() -> {
                if (ratingCarName  != null) ratingCarName.setText(car.name);
                if (ratingCarImage != null) ratingCarImage.setImageResource(CarAdapter.getCarImage(car.name));
            });
        });

        // Rating change listener
        if (ratingBar != null) {
            ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
                int stars = (int) rating;
                if (ratingLabel != null)
                    ratingLabel.setText(stars >= 0 && stars <= 5 ? LABELS[stars] : "اختر تقييمك");
            });
        }

        // Submit
        if (submitBtn != null) {
            submitBtn.setOnClickListener(v -> {
                if (ratingBar == null || ratingBar.getRating() == 0) {
                    Toast.makeText(this, "يرجى اختيار تقييم", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitBtn.setEnabled(false);
                String comment = commentInput != null && commentInput.getText() != null
                        ? commentInput.getText().toString().trim() : "";
                float stars = ratingBar.getRating();

                executor.execute(() -> {
                    RatingEntity rating = new RatingEntity();
                    rating.carId   = carId;
                    rating.userId  = 1;
                    rating.stars   = stars;
                    rating.comment = comment;
                    rating.date    = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    db.ratingDao().insertRating(rating);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "شكراً على تقييمك! ⭐", Toast.LENGTH_LONG).show();
                        finish();
                    });
                });
            });
        }
    }

    @Override
    protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
