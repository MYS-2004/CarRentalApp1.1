package com.carrental.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.CarEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<CarEntity>> carsLiveData  = new MutableLiveData<>();
    private final MutableLiveData<Boolean>         loadingData   = new MutableLiveData<>(false);
    private List<CarEntity> allCars = new ArrayList<>();

    // Active filter state
    private String  lastQuery    = "";
    private String  lastCategory = "";
    private int     lastMaxPrice = 999;
    private String  lastTrans    = "";
    private String  lastFuel     = "";
    private boolean lastAvailOnly = false;

    public CarViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        loadCars();
    }

    public LiveData<List<CarEntity>> getCars()    { return carsLiveData; }
    public LiveData<Boolean>         getLoading() { return loadingData;  }

    public void loadCars() {
        loadingData.setValue(true);
        executor.execute(() -> {
            allCars = db.carDao().getAllCars();
            if (allCars.isEmpty()) {
                insertSampleCars();
                allCars = db.carDao().getAllCars();
            }
            applyFiltersInternal();
            loadingData.postValue(false);
        });
    }

    public void filterCars(String query, String category, int maxPrice,
                           String transmission, String fuel, boolean availableOnly) {
        lastQuery    = query;
        lastCategory = category;
        lastMaxPrice = maxPrice;
        lastTrans    = transmission;
        lastFuel     = fuel;
        lastAvailOnly = availableOnly;
        executor.execute(this::applyFiltersInternal);
    }

    private void applyFiltersInternal() {
        List<CarEntity> filtered = new ArrayList<>();
        for (CarEntity car : allCars) {
            if (!lastQuery.isEmpty() &&
                !car.name.toLowerCase().contains(lastQuery.toLowerCase()) &&
                (car.model == null || !car.model.toLowerCase().contains(lastQuery.toLowerCase())))
                continue;
            if (!lastCategory.isEmpty() && !lastCategory.equals(car.model)) continue;
            if (lastMaxPrice < 999 && car.pricePerDay > lastMaxPrice)        continue;
            if (!lastTrans.isEmpty() && !lastTrans.equals(car.transmission)) continue;
            if (!lastFuel.isEmpty()  && !lastFuel.equals(car.fuelType))      continue;
            if (lastAvailOnly && !car.available)                              continue;
            filtered.add(car);
        }
        carsLiveData.postValue(filtered);
    }

    private void insertSampleCars() {
        Object[][] data = {
            {"Toyota Camry",   "عائلية",   65.0,  "بنزين",   "أوتوماتيك", 5, true},
            {"BMW X5",         "فاخرة",    150.0, "بنزين",   "أوتوماتيك", 5, true},
            {"Toyota Corolla", "اقتصادية", 40.0,  "بنزين",   "أوتوماتيك", 5, true},
            {"Jeep Wrangler",  "SUV",       110.0, "بنزين",   "يدوي",      5, true},
            {"Tesla Model 3",  "كهربائية", 130.0, "كهربائي", "أوتوماتيك", 5, true},
            {"Honda Civic",    "اقتصادية", 45.0,  "بنزين",   "أوتوماتيك", 5, true},
            {"Mercedes GLE",   "فاخرة",    200.0, "بنزين",   "أوتوماتيك", 7, true},
            {"Hyundai Tucson", "SUV",       80.0,  "بنزين",   "أوتوماتيك", 5, false},
        };
        for (Object[] d : data) {
            CarEntity car = new CarEntity();
            car.name         = (String)  d[0];
            car.model        = (String)  d[1];
            car.pricePerDay  = (double)  d[2];
            car.fuelType     = (String)  d[3];
            car.transmission = (String)  d[4];
            car.seats        = (int)     d[5];
            car.available    = (boolean) d[6];
            db.carDao().insertCar(car);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
