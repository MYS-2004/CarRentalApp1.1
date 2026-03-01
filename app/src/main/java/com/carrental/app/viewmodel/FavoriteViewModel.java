package com.carrental.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.CarEntity;
import com.carrental.app.data.FavoriteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<CarEntity>> favCarsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFavLiveData = new MutableLiveData<>(false);

    public FavoriteViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public LiveData<List<CarEntity>> getFavoriteCars() { return favCarsLiveData; }
    public LiveData<Boolean> getIsFavorite() { return isFavLiveData; }

    public void loadFavorites(int userId) {
        executor.execute(() -> {
            List<FavoriteEntity> favs = db.favoriteDao().getFavoritesByUser(userId);
            List<CarEntity> cars = new ArrayList<>();
            for (FavoriteEntity f : favs) {
                CarEntity c = db.carDao().getCarById(f.carId);
                if (c != null) cars.add(c);
            }
            favCarsLiveData.postValue(cars);
        });
    }

    public void checkFavorite(int userId, int carId) {
        executor.execute(() -> {
            List<FavoriteEntity> favs = db.favoriteDao().getFavoritesByUser(userId);
            boolean found = false;
            for (FavoriteEntity f : favs) { if (f.carId == carId) { found = true; break; } }
            isFavLiveData.postValue(found);
        });
    }

    public void toggleFavorite(int userId, int carId) {
        executor.execute(() -> {
            List<FavoriteEntity> favs = db.favoriteDao().getFavoritesByUser(userId);
            boolean found = false;
            for (FavoriteEntity f : favs) {
                if (f.carId == carId) { db.favoriteDao().deleteFavorite(f); found = true; break; }
            }
            if (!found) {
                FavoriteEntity fav = new FavoriteEntity();
                fav.userId = userId; fav.carId = carId;
                db.favoriteDao().insertFavorite(fav);
            }
            isFavLiveData.postValue(!found);
            loadFavorites(userId);
        });
    }
}
