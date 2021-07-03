package com.flexship.flexshipcookingass.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flexship.flexshipcookingass.models.Dish

@Database(entities = arrayOf(Dish::class),version = 1,exportSchema = false)
abstract class CookDatabase:RoomDatabase() {

    abstract fun provideCookDao():CookDao
}