package com.flexship.flexshipcookingass.models

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dish_table")
data class Dish(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val name: String = "",
    val recipe: String = "",
    val category: Int = 0,
    val image: Bitmap? = null,
    @ColumnInfo(name = "position_in_list") val positionInList: Int = 0
)