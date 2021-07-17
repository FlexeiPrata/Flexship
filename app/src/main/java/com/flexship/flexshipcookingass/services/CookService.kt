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
import com.flexship.flexshipcookingass.ui.other.MainActivity
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.other.Constans
import com.flexship.flexshipcookingass.other.Constans.ACTION_PAUSE
import com.flexship.flexshipcookingass.other.Constans.ACTION_PENDING_INTENT
import com.flexship.flexshipcookingass.other.Constans.ACTION_START_RESUME
import com.flexship.flexshipcookingass.other.Constans.ACTION_STOP
import com.flexship.flexshipcookingass.other.Constans.KEY_DISH_ID
import com.flexship.flexshipcookingass.other.Constans.KEY_POSITION_IN_LIST
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_CHANNEL_ID
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_CHANNEL_NAME
import com.flexship.flexshipcookingass.other.Constans.NOTIFICATION_ID
import com.flexship.flexshipcookingass.other.LOG_ID
import com.flexship.flexshipcookingass.other.zeroOrNotZero
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.Exception

@AndroidEntryPoint
class CookService : LifecycleService() {

    private var isFirstCooking = true
    private var isCanceled = false

    private var timeToCook = 0L

    //timer
    private var isTimerEnabled = false

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var currentNotificationBuilder: NotificationCompat.Builder


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_RESUME -> {
                    if (isFirstCooking) {
                        timeToCook = it.getLongExtra(Constans.KEY_TIME, 0)
                        currentDishId = it.getIntExtra(KEY_DISH_ID, 0)
                        posInList = it.getIntExtra(KEY_POSITION_IN_LIST, 0)
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



        isCooking.observe(this) {
            if(isWorking){
                updateNotification(it)
            }
        }
    }

    private fun updateNotification(isCooking: Boolean) {
        val notText = if (isCooking)
        {
            Log.d(LOG_ID,"UPDATE")
            "Pause"
        }
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
        var isCooking = MutableLiveData<Boolean>()
        val timer = MutableLiveData<Long>()
        var isWorking = false
        var currentDishId = 0
        var posInList = 0
    }

    private fun postInitialValues() {
        isCooking.postValue(false)
        timer.postValue(0L)
    }

    private fun runTimer() {
        isCooking.postValue(true)
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isCooking.value!!) {
                timeToCook -= Constans.DELAY_FOR_TIMER
                timer.value = timeToCook
                //PostValue по какой-то причине сбивал таймер в уведомлении, он показывал 00:09 - 00:08 - 00:06 и тд
                //Так, как сейчас он вроде работает правильно
                if (timeToCook <= 0L)
                    pauseService()
                delay(Constans.DELAY_FOR_TIMER)
            }
        }
    }

    private fun startForegroundService() {

        isFirstCooking = false
        isWorking = true
        //postInitialValues()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.breakfast)
            .setContentTitle("Currently cooking")
            .setContentText("00:00")
            .setContentIntent(getPendingIntent())

        currentNotificationBuilder = notificationBuilder

        runTimer()

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timer.observe(this) { time ->
            val notification = if (time > 0L)
            {
                currentNotificationBuilder.setContentText(
                    "${zeroOrNotZero(time / 1000 / 60)}:${zeroOrNotZero(time / 1000 % 60)}"
                )
            }
            else {
                currentNotificationBuilder.setContentText(
                    getString(R.string.notification_finished)
                )
            }
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }

    }

    private fun getPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).apply {
            putExtra(KEY_DISH_ID, currentDishId)
            putExtra(KEY_POSITION_IN_LIST, posInList)
            action = ACTION_PENDING_INTENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }


    private fun cancelService() {

        if(isWorking){
            isWorking = false
            isCanceled = true
            timer.removeObservers(this)
            isCooking.removeObservers(this)
            postInitialValues()
            stopForeground(true)
        }
        stopSelf()
    }

}