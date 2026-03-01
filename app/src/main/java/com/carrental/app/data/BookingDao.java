package com.carrental.app.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookingDao {
    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY id DESC")
    List<BookingEntity> getBookingsByUser(int userId);

    @Query("SELECT COUNT(*) FROM bookings WHERE userId = :userId AND carId = :carId")
    int hasUserBookedCar(int userId, int carId);

    @Insert
    long insertBooking(BookingEntity booking);

    @Update
    void updateBooking(BookingEntity booking);

    @Delete
    void deleteBooking(BookingEntity booking);
}
