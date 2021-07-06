package com.flexship.flexshipcookingass.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.flexship.flexshipcookingass.other.Constans.ACTION_PAUSE
import com.flexship.flexshipcookingass.other.Constans.ACTION_START_RESUME
import com.flexship.flexshipcookingass.other.Constans.ACTION_STOP
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_CHANNEL_ID
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_CHANNEL_NAME
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CookService :LifecycleService() {

    private var isFirstCooking = true

    @Inject
    lateinit var notificationBuilder :NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_RESUME ->{
                    if(isFirstCooking)
                        startForegroundService()


                }
                ACTION_PAUSE->{

                }
                ACTION_STOP->{

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun startForegroundService(){

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel=NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }
}