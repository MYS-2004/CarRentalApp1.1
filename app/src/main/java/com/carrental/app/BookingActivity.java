package com.carrental.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.BookingEntity;
import com.carrental.app.data.CarEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BookingActivity extends AppCompatActivity {
    private TextView startDateTV, endDateTV, totalPriceTV, daysCountTV, dailyPriceTV;
    private MaterialCardView startDateCard, endDateCard;
    private MaterialButton confirmBtn;
    private AppDatabase db;
    private CarEntity car;
    private String selectedStart = null, selectedEnd = null;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        startDateTV   = findViewById(R.id.startDate);
        endDateTV     = findViewById(R.id.endDate);
        totalPriceTV  = findViewById(R.id.totalPrice);
        daysCountTV   = findViewById(R.id.daysCount);
        dailyPriceTV  = findViewById(R.id.dailyPriceDisplay);
        startDateCard = findViewById(R.id.startDateCard);
        endDateCard   = findViewById(R.id.endDateCard);
        confirmBtn    = findViewById(R.id.confirmBooking);

        db = AppDatabase.getDatabase(getApplicationContext());

        int carId = getIntent().getIntExtra("carId", -1);
        if (carId == -1) { finish(); return; }

        // Back button
        if (findViewById(R.id.backButton) != null)
            findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Load car in background
        executor.execute(() -> {
            car = db.carDao().getCarById(carId);
            if (car == null) { runOnUiThread(this::finish); return; }
            runOnUiThread(() -> {
                if (dailyPriceTV != null) dailyPriceTV.setText((int) car.pricePerDay + " $");
                TextView nameTV  = findViewById(R.id.bookingCarName);
                TextView priceTV = findViewById(R.id.bookingCarPrice);
                if (nameTV  != null) nameTV.setText(car.name);
                if (priceTV != null) priceTV.setText((int) car.pricePerDay + " $ / يوم");
                android.widget.ImageView img = findViewById(R.id.bookingCarImage);
                if (img != null) img.setImageResource(CarAdapter.getCarImage(car.name));
            });
        });

        if (startDateCard != null) startDateCard.setOnClickListener(v -> pickDate(true));
        if (endDateCard   != null) endDateCard.setOnClickListener(v -> pickDate(false));
        if (confirmBtn    != null) confirmBtn.setOnClickListener(v -> confirmBooking());
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(y, m, d);
                    String dateStr = sdf.format(sel.getTime());
                    if (isStart) {
                        selectedStart = dateStr;
                        if (startDateTV != null) {
                            startDateTV.setText(dateStr);
                            startDateTV.setTextColor(getColor(R.color.text_primary));
                        }
                    } else {
                        selectedEnd = dateStr;
                        if (endDateTV != null) {
                            endDateTV.setText(dateStr);
                            endDateTV.setTextColor(getColor(R.color.text_primary));
                        }
                    }
                    recalculate();
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        // Min date = today
        dlg.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dlg.show();
    }

    private void recalculate() {
        if (selectedStart == null || selectedEnd == null || car == null) return;
        try {
            Date s = sdf.parse(selectedStart);
            Date e = sdf.parse(selectedEnd);
            if (s == null || e == null || !e.after(s)) {
                if (daysCountTV  != null) daysCountTV.setText("--");
                if (totalPriceTV != null) totalPriceTV.setText("--");
                return;
            }
            long days  = TimeUnit.MILLISECONDS.toDays(e.getTime() - s.getTime());
            double total = days * car.pricePerDay;
            if (daysCountTV  != null) daysCountTV.setText(days + " يوم");
            if (totalPriceTV != null) totalPriceTV.setText((int) total + " $");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void confirmBooking() {
        if (car == null) { Toast.makeText(this, "جارٍ التحميل...", Toast.LENGTH_SHORT).show(); return; }
        if (selectedStart == null || selectedEnd == null) {
            Toast.makeText(this, "يرجى اختيار تواريخ الحجز", Toast.LENGTH_SHORT).show(); return;
        }
        try {
            Date s = sdf.parse(selectedStart);
            Date e = sdf.parse(selectedEnd);
            if (s == null || e == null || !e.after(s)) {
                Toast.makeText(this, getString(R.string.error_date_invalid), Toast.LENGTH_SHORT).show(); return;
            }
            long days  = TimeUnit.MILLISECONDS.toDays(e.getTime() - s.getTime());
            double total = days * car.pricePerDay;

            if (confirmBtn != null) confirmBtn.setEnabled(false);

            final Date endDate = e;
            executor.execute(() -> {
                BookingEntity b = new BookingEntity();
                b.carId      = car.id;
                b.userId     = 1;
                b.startDate  = selectedStart;
                b.endDate    = selectedEnd;
                b.totalPrice = total;
                b.status     = "نشط";
                db.bookingDao().insertBooking(b);

                runOnUiThread(() -> {
                    NotificationHelper.showBookingConfirmed(this, car.name, selectedEnd);
                    NotificationHelper.scheduleExpiryReminder(this, car.id, endDate.getTime(), car.name);

                    Intent intent = new Intent(this, BookingConfirmationActivity.class);
                    intent.putExtra("carName",          car.name);
                    intent.putExtra("bookingDuration",  days + " يوم");
                    intent.putExtra("totalPrice",       (int) total);
                    intent.putExtra("carId",            car.id);
                    startActivity(intent);
                    finish();
                });
            });
        } catch (Exception ex) {
            if (confirmBtn != null) confirmBtn.setEnabled(true);
            Toast.makeText(this, getString(R.string.error_date_invalid), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
