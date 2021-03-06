package com.flexship.flexshipcookingass.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.other.Constants
import com.flexship.flexshipcookingass.other.Constants.ACTION_PAUSE
import com.flexship.flexshipcookingass.other.Constants.ACTION_PENDING_INTENT
import com.flexship.flexshipcookingass.other.Constants.ACTION_START_RESUME
import com.flexship.flexshipcookingass.other.Constants.ACTION_STOP
import com.flexship.flexshipcookingass.other.Constants.KEY_DISH_ID
import com.flexship.flexshipcookingass.other.Constants.KEY_POSITION_IN_LIST
import com.flexship.flexshipcookingass.other.Constants.NOTIFICATION_CHANNEL_ID
import com.flexship.flexshipcookingass.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.flexship.flexshipcookingass.other.Constants.NOTIFICATION_ID
import com.flexship.flexshipcookingass.other.LOG_ID
import com.flexship.flexshipcookingass.other.zeroOrNotZero
import com.flexship.flexshipcookingass.ui.other.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CookService : LifecycleService() {

    private var isFirstCooking = true
    private var isCanceled = false

    @Volatile
    private var timeToCook = 0L

    private lateinit var timerObject: CountDownTimer

    //timer
    private var isTimerEnabled = false

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var currentNotificationBuilder: NotificationCompat.Builder


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_RESUME -> {
                    if (isFirstCooking) {
                        timeToCook = it.getLongExtra(Constants.KEY_TIME, 0)
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

    }

    private fun updateNotification(isCooking: Boolean) {
        val notText = if (isCooking) {
            getString(R.string.pause)
        } else
            getString(R.string.continue_string)
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
        val timer = MutableLiveData<@kotlin.jvm.Volatile Long>()
        var isWorking = false
        var currentDishId = 0
        var posInList = 0
    }

    private fun postInitialValues() {
        isCooking.postValue(false)
        timer.value = 0
    }

    private fun runTimer() {
        isCooking.postValue(true)
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isCooking.value!!) {
                timeToCook -= Constants.DELAY_FOR_TIMER
                Log.d(LOG_ID, (timeToCook/1000).toString())
                timer.postValue(timeToCook)
                if (timeToCook <= 0L)
                    pauseService()
                delay(Constants.DELAY_FOR_TIMER)
            }
        }
       /* timerObject = object: CountDownTimer(timer.value ?: 0, Constants.DELAY_FOR_TIMER) {
            override fun onTick(millisUntilFinished: Long) {
                timer.value = millisUntilFinished
            }

            override fun onFinish() {
                pauseService()
            }
        }
        isCooking.observe(this) {
            if (isWorking) {
                updateNotification(it)
                if (!it) timerObject.cancel()
            }
        }
        timerObject.start()*/
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
            .setContentTitle(getString(R.string.currentlyCooking))
            .setContentText(getString(R.string.zeroTimer))
            .setContentIntent(getPendingIntent())

        currentNotificationBuilder = notificationBuilder

        runTimer()

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timer.observe(this) { time ->
            val notification = if (time > 0L) {
                currentNotificationBuilder.setContentText(
                    "${zeroOrNotZero(time / 1000 / 60)}:${zeroOrNotZero(time / 1000 % 60)}"
                )
            } else {
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

        if (isWorking) {
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