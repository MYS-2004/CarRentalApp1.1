package com.carrental.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class FilterActivity extends AppCompatActivity {

    private SeekBar priceSeekBar;
    private TextView priceRangeLabel;
    private ChipGroup transmissionGroup, fuelGroup;
    private SwitchMaterial availableSwitch;
    private MaterialButton applyBtn, resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        priceSeekBar     = findViewById(R.id.priceSeekBar);
        priceRangeLabel  = findViewById(R.id.priceRangeLabel);
        transmissionGroup = findViewById(R.id.transmissionGroup);
        fuelGroup        = findViewById(R.id.fuelGroup);
        availableSwitch  = findViewById(R.id.availableSwitch);
        applyBtn         = findViewById(R.id.applyFilterBtn);
        resetBtn         = findViewById(R.id.resetBtn);

        // Back button — both IDs for safety
        if (findViewById(R.id.backButton) != null)
            findViewById(R.id.backButton).setOnClickListener(v -> finish());

        if (priceSeekBar != null && priceRangeLabel != null) {
            priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                    priceRangeLabel.setText("0$ - " + p + "$");
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }

        if (applyBtn != null) {
            applyBtn.setOnClickListener(v -> applyFilter());
        }

        if (resetBtn != null) {
            resetBtn.setOnClickListener(v -> resetFilters());
        }
    }

    private void applyFilter() {
        Intent result = new Intent();
        result.putExtra("maxPrice",     priceSeekBar  != null ? priceSeekBar.getProgress() : 999);
        result.putExtra("availableOnly", availableSwitch != null && availableSwitch.isChecked());

        // Transmission
        String trans = "";
        if (transmissionGroup != null) {
            int transId = transmissionGroup.getCheckedChipId();
            if (transId == R.id.chipAutomatic) trans = "أوتوماتيك";
            else if (transId == R.id.chipManual) trans = "يدوي";
        }
        result.putExtra("transmission", trans);

        // Fuel
        String fuel = "";
        if (fuelGroup != null) {
            int fuelId = fuelGroup.getCheckedChipId();
            if (fuelId == R.id.chipGasoline) fuel = "بنزين";
            else if (fuelId == R.id.chipElectric) fuel = "كهربائي";
        }
        result.putExtra("fuelType", fuel);

        setResult(RESULT_OK, result);
        finish();
    }

    private void resetFilters() {
        if (priceSeekBar    != null) priceSeekBar.setProgress(300);
        if (priceRangeLabel != null) priceRangeLabel.setText("0$ - 300$");
        if (availableSwitch != null) availableSwitch.setChecked(false);
        if (transmissionGroup != null) transmissionGroup.clearCheck();
        if (fuelGroup != null) fuelGroup.clearCheck();
    }
}
