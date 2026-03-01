package com.carrental.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
    entities = {CarEntity.class, BookingEntity.class, UserEntity.class,
                FavoriteEntity.class, RatingEntity.class},
    version = 5,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract CarDao carDao();
    public abstract BookingDao bookingDao();
    public abstract UserDao userDao();
    public abstract FavoriteDao favoriteDao();
    public abstract RatingDao ratingDao();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class, "car_rental_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
