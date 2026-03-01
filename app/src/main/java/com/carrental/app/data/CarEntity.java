package com.carrental.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cars")
public class CarEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String model;      // يُستخدم كـ category أيضاً
    public double pricePerDay;
    public boolean available;
    public String fuelType;
    public String transmission;
    public int seats;
    public String imageUrl;
    public String description;
}
