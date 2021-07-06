package com.flexship.flexshipcookingass.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.flexship.flexshipcookingass.MainActivity
import com.flexship.flexshipcookingass.other.Constans
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {



    @Provides
    @ServiceScoped
    fun provideIntent(
        @ApplicationContext context: Context
    ) = Intent(context,MainActivity::class.java).apply {
        action=Constans.ACTION_PENDING_INTENT
    }

    @Provides
    @ServiceScoped
    fun providePendingIntent(
        @ApplicationContext context: Context,
        intent:Intent
    ) = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @Provides
    @ServiceScoped
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(context,Constans.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(true)
        .setOngoing(true)
        .setContentTitle("Currently cooking")
        .setContentText("00:00")
        .setContentIntent(pendingIntent)


}