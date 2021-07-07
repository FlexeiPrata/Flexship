package com.flexship.flexshipcookingass.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class CookService : LifecycleService() {

    private var isFirstCooking = true
    private var isCanceled = false

    private var timeToCook = 0L

    //timer
    private var timeStarted = 0L
    private var loopTime = 0L
    private var totalTime = 0L
    private var isTimerEnabled = false

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_RESUME -> {
                    if (isFirstCooking) {
                        startForegroundService()
                        timeToCook = it.getIntExtra(Constans.KEY_TIME, 0).toLong()
                    } else {
                        runTimer()
                    }
                }
                ACTION_PAUSE -> {
                    pauseService()
                }
                ACTION_STOP -> {

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
        timer.postValue(0)
    }

    private fun runTimer() {
        isCooking.postValue(true)
        isTimerEnabled = true
        timeStarted = System.currentTimeMillis()

        CoroutineScope(Dispatchers.Main).launch {
            while (isCooking.value!!) {
                if (timeToCook == totalTime) {
                    //stop timer
                }
                loopTime = System.currentTimeMillis() - timeStarted
                timer.postValue(loopTime + totalTime)

                delay(Constans.DELAY_FOR_TIMER)
            }
            totalTime += loopTime
        }
    }

    private fun startForegroundService() {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        runTimer()

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timer.observe(this){
            time->
            val notification=currentNotificationBuilder.setContentText()
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

//    private fun getFormattedTime(time:Int):String{
//        //time
//    }
}