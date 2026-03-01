package com.carrental.app.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CarDao {
    @Query("SELECT * FROM cars")
    List<CarEntity> getAllCars();

    @Query("SELECT * FROM cars WHERE id = :id")
    CarEntity getCarById(int id);

    @Insert
    void insertCar(CarEntity car);

    @Update
    void updateCar(CarEntity car);

    @Delete
    void deleteCar(CarEntity car);
}
