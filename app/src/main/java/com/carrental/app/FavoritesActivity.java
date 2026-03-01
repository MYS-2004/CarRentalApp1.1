package com.carrental.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carrental.app.viewmodel.FavoriteViewModel;

public class FavoritesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private CarAdapter adapter;
    private LinearLayout emptyFavorites;
    private TextView favCount;
    private FavoriteViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView   = findViewById(R.id.recyclerViewFavorites);
        emptyFavorites = findViewById(R.id.emptyFavorites);
        favCount       = findViewById(R.id.favCount);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(false);
        }

        adapter = new CarAdapter(new java.util.ArrayList<>(), car -> {
            Intent intent = new Intent(this, CarDetailsActivity.class);
            intent.putExtra("carId", car.id);
            startActivity(intent);
        });
        if (recyclerView != null) recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
        viewModel.getFavoriteCars().observe(this, cars -> {
            if (favCount != null) favCount.setText(cars.size() + " سيارة");
            if (cars.isEmpty()) {
                if (emptyFavorites != null) emptyFavorites.setVisibility(View.VISIBLE);
                if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            } else {
                if (emptyFavorites != null) emptyFavorites.setVisibility(View.GONE);
                if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
            }
            adapter.updateList(cars);
        });

        if (findViewById(R.id.backButton) != null)
            findViewById(R.id.backButton).setOnClickListener(v -> finish());

        setupBottomNav(R.id.nav_favorites);
        viewModel.loadFavorites(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadFavorites(1); // Refresh when returning from car details
    }
}
