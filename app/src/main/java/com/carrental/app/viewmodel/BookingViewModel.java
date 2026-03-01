package com.carrental.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.carrental.app.data.AppDatabase;
import com.carrental.app.data.BookingEntity;
import com.carrental.app.data.CarEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<BookingEntity>> bookingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bookingSavedLiveData = new MutableLiveData<>();

    public BookingViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public LiveData<List<BookingEntity>> getBookings() { return bookingsLiveData; }
    public LiveData<Boolean> getBookingSaved() { return bookingSavedLiveData; }

    public void loadBookingsByUser(int userId) {
        executor.execute(() -> {
            List<BookingEntity> list = db.bookingDao().getBookingsByUser(userId);
            bookingsLiveData.postValue(list);
        });
    }

    public void saveBooking(BookingEntity booking) {
        executor.execute(() -> {
            db.bookingDao().insertBooking(booking);
            bookingSavedLiveData.postValue(true);
        });
    }

    public CarEntity getCarById(int id) {
        return db.carDao().getCarById(id);
    }
}
