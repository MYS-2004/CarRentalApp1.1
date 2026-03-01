package com.carrental.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "ratings",
        foreignKeys = @ForeignKey(entity = CarEntity.class,
                parentColumns = "id", childColumns = "carId",
                onDelete = ForeignKey.CASCADE))
public class RatingEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int carId;
    public int userId;
    public float stars;
    public String comment;
    public String date;
}
