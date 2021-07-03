package com.flexship.flexshipcookingass.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.flexship.flexshipcookingass.db.CookDatabase
import com.flexship.flexshipcookingass.other.Constans
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
        Constans.DATABASE_NAME
    )

    @Provides
    @Singleton
    fun provideCookDao(
        cookDatabase: CookDatabase
    ) = cookDatabase.provideCookDao()



}