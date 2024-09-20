package com.skysphere.skysphere.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skysphere.skysphere.API.RetrofitInstance
import com.skysphere.skysphere.API.WeatherData
import com.skysphere.skysphere.GPSManager
import com.skysphere.skysphere.notifications.NotificationManager
import com.skysphere.skysphere.ui.settings.SettingsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val gpsManager = GPSManager(applicationContext)
            var latitude = 0.0
            var longitude = 0.0

            gpsManager.getCurrentLocation(object : GPSManager.GPSManagerCallback {
                override fun onLocationRetrieved(lat: Double, lon: Double, addressDetails: String?) {
                    latitude = lat
                    longitude = lon
                }

                override fun onLocationError(error: String) {
                    // Handle error
                }
            })

            val weatherData = fetchWeatherData(latitude, longitude)
            if (isSevereWeather(weatherData) && isNotificationEnabled()) {
                NotificationManager.showSevereWeatherNotification(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun isNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(SettingsFragment.SEVERE_NOTIFICATION_PREFERENCE_KEY, false)
    }

    private suspend fun fetchWeatherData(latitude: Double, longitude: Double): WeatherData {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.instance.getWeatherData(
                latitude, longitude, "weather_code", "weather_code", "auto", ""
            ).execute().body() ?: throw Exception("Failed to fetch weather data")
        }
    }

    private fun isSevereWeather(weatherData: WeatherData): Boolean {
        val severeWeatherCodes = listOf(95, 96, 99) // Thunderstorm codes
        return weatherData.current.weather_code in severeWeatherCodes
    }
}