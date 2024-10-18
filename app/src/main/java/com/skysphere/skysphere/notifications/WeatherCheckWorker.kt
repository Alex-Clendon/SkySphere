package com.skysphere.skysphere.notifications

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.Observer
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skysphere.skysphere.GPSManager
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.weather.WeatherResults
import com.skysphere.skysphere.ui.settings.SettingsFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
@RequiresApi(Build.VERSION_CODES.O)
class WeatherCheckWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val viewModel: WeatherViewModel,
    private val settingsManager: SettingsManager
) : CoroutineWorker(context, params) {

    // Fetch weather data and check if it's severe, sending a notification if it is
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val weatherDataDeferred = CompletableDeferred<WeatherResults?>()

        // Observer to retrieve weather data
        val observer = Observer<WeatherResults?> { results ->
            weatherDataDeferred.complete(results)
        }

        // Observe the LiveData
        viewModel.weatherResults.observeForever(observer)

        try {
            // Get the current location
            val gpsManager = GPSManager(applicationContext)

            // Trigger fetching weather data
            viewModel.fetchWeatherData() // This will update the LiveData

            // Wait for the weather data
            val weatherResults = weatherDataDeferred.await() // Wait for the result
            viewModel.weatherResults.removeObserver(observer) // Clean up observer

            if (isSevereWeather(weatherResults) && isNotificationEnabled()) {
                NotificationManager.showSevereWeatherNotification(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            viewModel.weatherResults.removeObserver(observer) // Clean up on error as well
            Result.failure()
        }
    }

    // Make sure that the notifications are enabled from settings.
    private fun isNotificationEnabled(): Boolean {
        return settingsManager.checkNotification(SettingsFragment.SEVERE_NOTIFICATION_PREFERENCE_KEY, true)
    }


    // Check if the weather code is one of the severe codes.
    private fun isSevereWeather(weatherData: WeatherResults?): Boolean {
        val severeWeatherCodes = listOf(95, 96, 99) // Thunderstorm codes
        return weatherData?.current?.weatherCode in severeWeatherCodes
    }
}