package com.skysphere.skysphere.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.skysphere.skysphere.notifications.WeatherCheckWorker
import java.util.concurrent.TimeUnit

class WeatherService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scheduleWeatherCheck()
        return START_STICKY
    }

    private fun scheduleWeatherCheck() {
        val weatherCheckRequest = PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "weatherCheck",
                ExistingPeriodicWorkPolicy.UPDATE,
                weatherCheckRequest
            )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}