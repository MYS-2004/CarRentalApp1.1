package com.carrental.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public static class BookingItem {
        public String carName;
        public String startDate;
        public String endDate;
        public double price;
        public String status;
        public int carId;

        public BookingItem(String carName, String startDate, String endDate, double price, String status, int carId) {
            this.carName = carName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.price = price;
            this.status = status;
            this.carId = carId;
        }
    }

    private final List<BookingItem> bookingList;
    private final Context context;

    public BookingAdapter(List<BookingItem> bookingList, Context context) {
        this.bookingList = bookingList;
        this.context = context;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingItem booking = bookingList.get(position);

        holder.carName.setText(booking.carName);
        holder.dates.setText(booking.startDate + "  →  " + booking.endDate);
        holder.price.setText((int) booking.price + " $");

        // Status badge
        boolean isActive = "نشط".equals(booking.status);
        holder.statusBadge.setText(isActive ? "نشط ✓" : "منتهي");
        holder.statusBadge.setBackgroundResource(isActive ?
                R.drawable.badge_available : R.drawable.badge_unavailable);
        holder.statusBadge.setTextColor(isActive ? 0xFF0A0E1A : 0xFFFFFFFF);

        // زر التقييم يظهر فقط للمنتهية
        if (!isActive) {
            holder.rateBtn.setVisibility(View.VISIBLE);
            holder.rateBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, RatingActivity.class);
                intent.putExtra("carId", booking.carId);
                context.startActivity(intent);
            });
        } else {
            holder.rateBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return bookingList.size(); }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView carName, dates, price, statusBadge;
        MaterialButton rateBtn;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            carName = itemView.findViewById(R.id.textCarName);
            dates = itemView.findViewById(R.id.textDates);
            price = itemView.findViewById(R.id.textPrice);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            rateBtn = itemView.findViewById(R.id.rateBtn);
        }
    }
}
