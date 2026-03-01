package com.carrental.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.FavoriteEntity;
import com.carrental.app.data.CarEntity;
import com.google.android.material.button.MaterialButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<CarEntity> carList;
    private final OnCarClickListener listener;
    private final Set<Integer> favoritedIds = new HashSet<>();
    private final ExecutorService executor  = Executors.newSingleThreadExecutor();
    private static final int USER_ID = 1;

    public interface OnCarClickListener {
        void onCarClick(CarEntity car);
    }

    public CarAdapter(List<CarEntity> carList, OnCarClickListener listener) {
        this.carList  = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_item, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        CarEntity car     = carList.get(position);
        Context   context = holder.itemView.getContext();

        // Text fields
        if (holder.name     != null) holder.name.setText(car.name);
        if (holder.price    != null) holder.price.setText((int) car.pricePerDay + " $");
        if (holder.model    != null) holder.model.setText(car.model);
        if (holder.seats    != null) holder.seats.setText("👤 " + (car.seats > 0 ? car.seats : 5));
        if (holder.fuel     != null) holder.fuel.setText("كهربائي".equals(car.fuelType) ? "⚡" : "⛽");
        if (holder.image    != null) holder.image.setImageResource(getCarImage(car.name));

        // Availability badge
        if (holder.availBadge != null) {
            if (car.available) {
                holder.availBadge.setText("متاحة");
                holder.availBadge.setBackgroundResource(R.drawable.badge_available);
                holder.availBadge.setTextColor(0xFF0A0E1A);
            } else {
                holder.availBadge.setText("محجوزة");
                holder.availBadge.setBackgroundResource(R.drawable.badge_unavailable);
                holder.availBadge.setTextColor(0xFFFFFFFF);
            }
        }

        // Card click → details
        holder.itemView.setOnClickListener(v -> listener.onCarClick(car));

        // Quick book button
        if (holder.quickBookBtn != null) {
            if (car.available) {
                holder.quickBookBtn.setText("احجز");
                holder.quickBookBtn.setAlpha(1f);
                holder.quickBookBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(context, BookingActivity.class);
                    intent.putExtra("carId", car.id);
                    context.startActivity(intent);
                });
            } else {
                holder.quickBookBtn.setText("محجوزة");
                holder.quickBookBtn.setAlpha(0.5f);
                holder.quickBookBtn.setEnabled(false);
            }
        }

        // Favorite button — works directly from the list
        if (holder.favButton != null) {
            boolean isFav = favoritedIds.contains(car.id);
            holder.favButton.setIconResource(isFav ?
                    android.R.drawable.btn_star_big_on :
                    android.R.drawable.btn_star_big_off);
            holder.favButton.setIconTint(android.content.res.ColorStateList.valueOf(
                    isFav ? 0xFFE8FF00 : 0xFF8B9CC8));

            holder.favButton.setOnClickListener(v -> {
                AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
                boolean wasFav = favoritedIds.contains(car.id);
                executor.execute(() -> {
                    if (wasFav) {
                        List<FavoriteEntity> favs = db.favoriteDao().getFavoritesByUser(USER_ID);
                        for (FavoriteEntity f : favs) {
                            if (f.carId == car.id) { db.favoriteDao().deleteFavorite(f); break; }
                        }
                        favoritedIds.remove(car.id);
                    } else {
                        FavoriteEntity fav = new FavoriteEntity();
                        fav.userId = USER_ID; fav.carId = car.id;
                        db.favoriteDao().insertFavorite(fav);
                        favoritedIds.add(car.id);
                    }
                    // Update icon on UI thread
                    int adapterPos = holder.getAdapterPosition();
                    if (adapterPos != RecyclerView.NO_ID) {
                        holder.itemView.post(() -> notifyItemChanged(adapterPos));
                    }
                    holder.itemView.post(() ->
                            Toast.makeText(context,
                                    wasFav ? "تم الإزالة من المفضلة" : "تمت الإضافة للمفضلة ❤️",
                                    Toast.LENGTH_SHORT).show());
                });
            });
        }
    }

    public static int getCarImage(String carName) {
        if (carName == null) return R.drawable.car_default;
        String lower = carName.toLowerCase();
        if (lower.contains("bmw") || lower.contains("x5"))        return R.drawable.car_bmw;
        if (lower.contains("tesla") || lower.contains("model"))   return R.drawable.car_tesla;
        if (lower.contains("mercedes") || lower.contains("gle"))  return R.drawable.car_mercedes;
        if (lower.contains("camry") || lower.contains("corolla")
                || lower.contains("toyota"))                       return R.drawable.car_camry;
        return R.drawable.car_default;
    }

    @Override public int getItemCount() { return carList.size(); }

    public void updateList(List<CarEntity> newList) {
        carList = newList;
        notifyDataSetChanged();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, model, seats, fuel, availBadge;
        ImageView image;
        MaterialButton quickBookBtn, favButton;

        CarViewHolder(View v) {
            super(v);
            name       = v.findViewById(R.id.carName);
            price      = v.findViewById(R.id.carPrice);
            model      = v.findViewById(R.id.carModel);
            seats      = v.findViewById(R.id.carSeats);
            fuel       = v.findViewById(R.id.carFuel);
            availBadge = v.findViewById(R.id.availabilityBadge);
            image      = v.findViewById(R.id.carImage);
            quickBookBtn = v.findViewById(R.id.quickBookBtn);
            favButton    = v.findViewById(R.id.favButton);
        }
    }
}
