package com.flexship.flexshipcookingass.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.Stages

@Database(entities = arrayOf(Dish::class,Stages::class),version = 1,exportSchema = false)
@TypeConverters(Converters::class)
abstract class CookDatabase:RoomDatabase() {

    abstract fun provideCookDao() : CookDao
}