package com.flexship.flexshipcookingass.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.other.Constans
import com.flexship.flexshipcookingass.other.Constans.ACTION_PAUSE
import com.flexship.flexshipcookingass.other.Constans.ACTION_START_RESUME
import com.flexship.flexshipcookingass.other.Constans.ACTION_STOP
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_CHANNEL_ID
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_CHANNEL_NAME
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_ID
import com.flexship.flexshipcookingass.other.zeroOrNotZero
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class CookService : LifecycleService() {

    private var isFirstCooking = true
    private var isCanceled = false

    private var timeToCook = 0L

    //timer
    private var isTimerEnabled = false

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_RESUME -> {
                    if (isFirstCooking) {
                        timeToCook = it.getLongExtra(Constans.KEY_TIME, 0)
                        startForegroundService()
                    } else {
                        runTimer()
                    }
                }
                ACTION_PAUSE -> {
                    pauseService()
                }
                ACTION_STOP -> {
                    cancelService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isCooking.postValue(false)
        isTimerEnabled = false
    }

    override fun onCreate() {
        super.onCreate()

        currentNotificationBuilder=notificationBuilder

        postInitialValues()

        isCooking.observe(this) {
            updateNotification(it)
        }
    }

    private fun updateNotification(isCooking: Boolean) {
        val notText = if (isCooking)
            "Pause"
        else
            "Resume"
        val pendingIntent = if (isCooking) {
            val pauseIntent = Intent(this, CookService::class.java).apply {
                action = ACTION_PAUSE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, CookService::class.java).apply {
                action = ACTION_START_RESUME
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!isCanceled) {
            currentNotificationBuilder = notificationBuilder
                .addAction(R.drawable.ic_pause, notText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }


    companion object {
        val isCooking = MutableLiveData<Boolean>()
        val timer = MutableLiveData<Long>()
    }

    private fun postInitialValues() {
        isCooking.postValue(false)
        //timer.postValue(0)
    }

    private fun runTimer() {
        isCooking.postValue(true)
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isCooking.value!!) {
                timeToCook-=1000L
                timer.postValue(timeToCook)
                if(timeToCook==0L)
                    pauseService()
                delay(Constans.DELAY_FOR_TIMER)
            }
        }
    }

    private fun startForegroundService() {

        isFirstCooking=false

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        runTimer()

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timer.observe(this) { time ->
            val notification = currentNotificationBuilder.setContentText(
                "${zeroOrNotZero(time / 1000 / 60)}:${
                    zeroOrNotZero(time / 1000 % 60)
                }"
            )
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }


    private fun cancelService(){
        isCooking.postValue(false)
        isTimerEnabled=false
        isCanceled=true
        stopForeground(true)
        stopSelf()
    }
}