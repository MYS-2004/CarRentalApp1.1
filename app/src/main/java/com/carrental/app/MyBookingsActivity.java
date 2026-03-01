package com.carrental.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.BookingEntity;
import com.carrental.app.data.CarEntity;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyBookingsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private AppDatabase db;
    private TabLayout tabLayout;
    private LinearLayout emptyState;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String currentStatus = "نشط";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        recyclerView = findViewById(R.id.recyclerViewBookings);
        tabLayout    = findViewById(R.id.bookingTabs);
        emptyState   = findViewById(R.id.emptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        db = AppDatabase.getDatabase(getApplicationContext());

        if (findViewById(R.id.backButton) != null)
            findViewById(R.id.backButton).setOnClickListener(v -> finish());

        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.active_bookings)));
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.past_bookings)));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override public void onTabSelected(TabLayout.Tab tab) {
                    currentStatus = tab.getPosition() == 0 ? "نشط" : "منتهي";
                    loadBookings(currentStatus);
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        setupBottomNav(R.id.nav_bookings);
        loadBookings("نشط");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto-update expired bookings, then refresh
        executor.execute(() -> {
            updateExpiredBookings();
            runOnUiThread(() -> loadBookings(currentStatus));
        });
    }

    /** تحديث الحجوزات المنتهية تلقائياً بناءً على تاريخ الإرجاع */
    private void updateExpiredBookings() {
        String today = sdf.format(new Date());
        List<BookingEntity> all = db.bookingDao().getBookingsByUser(1);
        for (BookingEntity b : all) {
            if ("نشط".equals(b.status) && b.endDate != null && b.endDate.compareTo(today) < 0) {
                b.status = "منتهي";
                db.bookingDao().updateBooking(b);
            }
        }
    }

    private void loadBookings(String status) {
        executor.execute(() -> {
            List<BookingEntity> entities = db.bookingDao().getBookingsByUser(1);
            List<BookingAdapter.BookingItem> items = new ArrayList<>();
            for (BookingEntity e : entities) {
                if (status.equals(e.status)) {
                    CarEntity car = db.carDao().getCarById(e.carId);
                    String carName = car != null ? car.name : "سيارة";
                    items.add(new BookingAdapter.BookingItem(
                            carName, e.startDate, e.endDate, e.totalPrice, e.status, e.carId));
                }
            }
            runOnUiThread(() -> {
                if (emptyState != null)
                    emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
                bookingAdapter = new BookingAdapter(items, this);
                recyclerView.setAdapter(bookingAdapter);
            });
        });
    }

    @Override
    protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
