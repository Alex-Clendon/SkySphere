package com.skysphere.skysphere.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.skysphere.skysphere.ui.settings.SettingsFragment
import java.util.concurrent.TimeUnit

class WeatherService : Service() {

    companion object {
        private const val WEATHER_CHECK_WORK_NAME = "weatherCheck"

        fun startWeatherMonitoring(context: Context) {
            val intent = Intent(context, WeatherService::class.java)
            context.startService(intent)
        }

        fun stopWeatherMonitoring(context: Context) {
            val intent = Intent(context, WeatherService::class.java)
            context.stopService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isNotificationEnabled = sharedPreferences.getBoolean(SettingsFragment.SEVERE_NOTIFICATION_PREFERENCE_KEY, false)

        if (isNotificationEnabled) {
            scheduleWeatherCheck()
        } else {
            cancelWeatherCheck()
            stopSelf()
        }

        return START_STICKY
    }

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

    private fun cancelWeatherCheck() {
        WorkManager.getInstance(applicationContext).cancelUniqueWork(WEATHER_CHECK_WORK_NAME)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}