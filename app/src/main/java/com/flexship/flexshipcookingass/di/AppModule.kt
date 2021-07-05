package com.flexship.flexshipcookingass.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.flexship.flexshipcookingass.db.CookDatabase
import com.flexship.flexshipcookingass.other.Constans
import com.flexship.flexshipcookingass.other.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context:Context
    ) = Room.databaseBuilder(
        context,
        CookDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideCookDao(
        cookDatabase: CookDatabase
    ) = cookDatabase.provideCookDao()


    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ) = context.getSharedPreferences(Constans.SHARED_PREF_NAME,Context.MODE_PRIVATE)



}