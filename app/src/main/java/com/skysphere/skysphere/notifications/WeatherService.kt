package com.skysphere.skysphere.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.ui.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WeatherService : Service() {

    @Inject
    lateinit var settingsManager: SettingsManager

    companion object {
        // Including the work name here
        private const val WEATHER_CHECK_WORK_NAME = "weatherCheck"

        // Including the start and stop functions for the worker here
        fun startWeatherMonitoring(context: Context) {
            val intent = Intent(context, WeatherService::class.java)
            context.startService(intent)
        }

        fun stopWeatherMonitoring(context: Context) {
            val intent = Intent(context, WeatherService::class.java)
            context.stopService(intent)
        }
    }

    // Including the onCreate function here, if notifications are enabled, start the worker
    // If they are disabled, cancel the worker if it was ever running.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Check to make sure user has either severe or rain notifications enabled
        val forSevere = settingsManager.checkNotification(SettingsFragment.SEVERE_NOTIFICATION_PREFERENCE_KEY, true)
        val forRain = settingsManager.checkNotification(SettingsFragment.RAIN_FORECAST_NOTIFICATION_PREFERENCE_KEY, true)

        if (forSevere || forRain) {
            scheduleWeatherCheck()
        } else {
            cancelWeatherCheck()
            stopSelf()
        }
        return START_STICKY
    }

    // Including the schedule and cancel functions here
    // Creates a work request to check the weather every 15 minutes
    private fun scheduleWeatherCheck() {
        val weatherCheckRequest = PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                WEATHER_CHECK_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                weatherCheckRequest
            )
    }

    // Cancel the worker if it was ever running
    private fun cancelWeatherCheck() {
        WorkManager.getInstance(applicationContext).cancelUniqueWork(WEATHER_CHECK_WORK_NAME)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}