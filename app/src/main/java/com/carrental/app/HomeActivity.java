package com.carrental.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carrental.app.viewmodel.CarViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class HomeActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private CarAdapter adapter;
    private CarViewModel viewModel;
    private TextView carsCount;
    private ChipGroup categoriesLayout;

    private int    maxPrice           = 999;
    private String filterTransmission = "";
    private String filterFuel         = "";
    private boolean availableOnly     = false;
    private String currentQuery       = "";

    private final ActivityResultLauncher<Intent> filterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    maxPrice           = result.getData().getIntExtra("maxPrice", 999);
                    availableOnly      = result.getData().getBooleanExtra("availableOnly", false);
                    filterTransmission = result.getData().getStringExtra("transmission");
                    filterFuel         = result.getData().getStringExtra("fuelType");
                    if (filterTransmission == null) filterTransmission = "";
                    if (filterFuel         == null) filterFuel         = "";
                    applyFilters();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView     = findViewById(R.id.recyclerViewCars);
        carsCount        = findViewById(R.id.carsCount);
        categoriesLayout = findViewById(R.id.categoriesLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Performance optimizations
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemViewCacheSize(20);

        // Show username from prefs
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        TextView userNameTV = findViewById(R.id.userName);
        if (userNameTV != null)
            userNameTV.setText("مرحباً، " + prefs.getString("userName", "المستخدم"));

        // Adapter
        adapter = new CarAdapter(new java.util.ArrayList<>(), car -> {
            Intent intent = new Intent(HomeActivity.this, CarDetailsActivity.class);
            intent.putExtra("carId", car.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(CarViewModel.class);
        viewModel.getCars().observe(this, cars -> {
            adapter.updateList(cars);
            if (carsCount != null) carsCount.setText(cars.size() + " سيارة");
        });

        // Search
        TextInputEditText searchBar = findViewById(R.id.searchBar);
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
                @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    currentQuery = s.toString();
                    applyFilters();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Category chips
        if (categoriesLayout != null)
            categoriesLayout.setOnCheckedStateChangeListener((group, ids) -> applyFilters());

        // Filter button
        if (findViewById(R.id.filterBtn) != null)
            findViewById(R.id.filterBtn).setOnClickListener(v ->
                    filterLauncher.launch(new Intent(this, FilterActivity.class)));

        // Notification bell → My Bookings
        if (findViewById(R.id.notifBtn) != null)
            findViewById(R.id.notifBtn).setOnClickListener(v ->
                    startActivity(new Intent(this, MyBookingsActivity.class)));

        setupBottomNav(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh username in case profile was updated
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        TextView userNameTV = findViewById(R.id.userName);
        if (userNameTV != null)
            userNameTV.setText("مرحباً، " + prefs.getString("userName", "المستخدم"));
    }

    private String getCurrentCategory() {
        if (categoriesLayout == null) return "";
        int id = categoriesLayout.getCheckedChipId();
        if (id == R.id.categoryFamily)  return "عائلية";
        if (id == R.id.categorySUV)     return "SUV";
        if (id == R.id.categoryEconomy) return "اقتصادية";
        if (id == R.id.categoryLuxury)  return "فاخرة";
        if (id == R.id.categoryElectric) return "كهربائية";
        return "";
    }

    private void applyFilters() {
        viewModel.filterCars(currentQuery, getCurrentCategory(),
                maxPrice, filterTransmission, filterFuel, availableOnly);
    }
}
