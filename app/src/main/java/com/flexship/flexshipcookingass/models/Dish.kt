package com.flexship.flexshipcookingass.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dish_table")
data class Dish(
    val name:String,
    val recipe:String,
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null
)