package com.flexship.flexshipcookingass.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dish_table")
data class Dish(
    @PrimaryKey(autoGenerate = true) var id : Int,
    val name : String,
    val recipe : String,
    val category: Int,
    @ColumnInfo (name = "position_in_list") val positionInList: Int
)