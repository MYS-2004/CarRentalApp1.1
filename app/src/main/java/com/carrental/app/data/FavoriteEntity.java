package com.carrental.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
    @Entity(tableName = "favorites",
            foreignKeys = @ForeignKey(entity = CarEntity.class,
                    parentColumns = "id",
                    childColumns = "carId",
                    onDelete = ForeignKey.CASCADE))
    public class FavoriteEntity {
        @PrimaryKey(autoGenerate = true)
        public int id;

        public int carId;
        public int userId;
    }


