package com.carrental.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookings",
        foreignKeys = @ForeignKey(entity = CarEntity.class,
                parentColumns = "id",
                childColumns = "carId",
                onDelete = ForeignKey.CASCADE))
public class BookingEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int carId;
    public int userId;
    public String startDate;
    public String endDate;
    public double totalPrice;
    public String status; // "نشط" أو "منتهي"
}
