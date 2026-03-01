package com.carrental.app.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RatingDao {
    @Insert
    void insertRating(RatingEntity rating);

    @Query("SELECT * FROM ratings WHERE carId = :carId")
    List<RatingEntity> getRatingsByCar(int carId);

    @Query("SELECT AVG(stars) FROM ratings WHERE carId = :carId")
    float getAverageRating(int carId);
}
