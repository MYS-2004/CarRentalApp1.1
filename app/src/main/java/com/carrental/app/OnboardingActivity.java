package com.carrental.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 pager;
    private MaterialButton nextBtn, skipBtn;
    private LinearLayout dotsContainer;
    private int currentPage = 0;

    private final String[] icons = {"🚗", "📅", "🔔"};
    private final String[] titles = {
            "تصفح السيارات",
            "احجز بكل سهولة",
            "تنبيهات ذكية"
    };
    private final String[] descs = {
            "استعرض مئات السيارات من مختلف الفئات\nواختر ما يناسب احتياجاتك",
            "حدد تاريخ الاستلام والإرجاع\nواحصل على السعر الإجمالي فوراً",
            "نذكّرك قبل انتهاء مدة الاستئجار\nحتى لا تفوّتك أي تفاصيل"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        pager = findViewById(R.id.onboardingPager);
        nextBtn = findViewById(R.id.nextBtn);
        skipBtn = findViewById(R.id.skipBtn);
        dotsContainer = findViewById(R.id.dotsContainer);

        pager.setAdapter(new OnboardingAdapter());
        setupDots(0);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                setupDots(position);
                nextBtn.setText(position == 2 ? "ابدأ الآن" : "التالي");
            }
        });

        nextBtn.setOnClickListener(v -> {
            if (currentPage < 2) {
                pager.setCurrentItem(currentPage + 1, true);
            } else {
                goToLogin();
            }
        });

        skipBtn.setOnClickListener(v -> goToLogin());
    }

    private void goToLogin() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putBoolean("onboardingDone", true).apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setupDots(int active) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            int size = i == active ? 24 : 8;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (getResources().getDisplayMetrics().density * (i == active ? 24 : 8)),
                    (int) (getResources().getDisplayMetrics().density * 8));
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == active ? R.drawable.dot_accent : R.drawable.dot_inactive);
            dotsContainer.addView(dot);
        }
    }

    class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.icon.setText(icons[position]);
            holder.title.setText(titles[position]);
            holder.desc.setText(descs[position]);
        }

        @Override
        public int getItemCount() { return 3; }

        class VH extends RecyclerView.ViewHolder {
            TextView icon, title, desc;
            VH(View v) {
                super(v);
                icon = v.findViewById(R.id.onboardIcon);
                title = v.findViewById(R.id.onboardTitle);
                desc = v.findViewById(R.id.onboardDesc);
            }
        }
    }
}
