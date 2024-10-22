package com.skysphere.skysphere.notifications

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.weather.WeatherResults
import com.skysphere.skysphere.ui.settings.SettingsFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.log

@HiltWorker
@RequiresApi(Build.VERSION_CODES.O)
class WeatherCheckWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val viewModel: WeatherViewModel,
    private val settingsManager: SettingsManager
) : CoroutineWorker(context, params) {


    private var lastRainForecastTimestamp = LocalDateTime.now()
    private var hasDailyBeenSent = false



    private var weatherResults: WeatherResults? = null
    // Fetch weather data and check if it's severe, sending a notification if it is
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
       try {
           // Initialize weather results directly from viewModel
           weatherResults = viewModel.getData()

           // If the weather code is severe (thunderstorm) and notification is enabled, show notification
           if (isSevereWeather(weatherResults) && isNotificationEnabled(SettingsFragment.SEVERE_NOTIFICATION_PREFERENCE_KEY)) {
               NotificationManager.showSevereWeatherNotification(applicationContext,
                   weatherResults?.current?.weatherCode
               )
           }

           // If the rain forecast is not null (rain forecasted) and notification is enabled, show notification
           val rainForecast = checkRainForecast(weatherResults)
           if (rainForecast != null && isNotificationEnabled(SettingsFragment.RAIN_FORECAST_NOTIFICATION_PREFERENCE_KEY)) {
               NotificationManager.showRainForecastNotification(applicationContext, rainForecast)
           }

           // If the daily summary has not been sent from 7:00 - 7:59 and notification is enabled, show notification
           val currentTime = LocalDateTime.now()
           if(currentTime.hour == 15 && isNotificationEnabled(SettingsFragment.DAILY_SUMMARY_NOTIFICATION_PREFERENCE_KEY)){
                if(!hasDailyBeenSent){
                    NotificationManager.showDailySummaryNotification(applicationContext, weatherResults)
                    hasDailyBeenSent = true
                }
           }



            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    // Make sure that the notifications are enabled from settings.
    private fun isNotificationEnabled(key: String): Boolean {
        return settingsManager.checkNotification(key, false)
    }


    // Check if the weather code is one of the severe codes.
    private fun isSevereWeather(weatherData: WeatherResults?): Boolean {

        /* Production code (REVERT BACK TO THIS AFTER TESTING)
        val severeWeatherCodes = listOf(95, 96, 99) // Thunderstorm codes
        return weatherData?.current?.weatherCode in severeWeatherCodes
        */
        return weatherData?.current?.weatherCode in listOf(0, 1, 2, 3)

    }

    private fun checkRainForecast(weatherData: WeatherResults?): Pair<Int?, String>? {
        val hourlyCodes = weatherData?.hourly?.weatherCode?.take(24) // 24 hours of the day
        val hourlyTimes = weatherData?.hourly?.time?.take(24) // 24 hours of the day

        val rainCodes = listOf(0, 1, 2, 3) // rain codes (DUMMY VALUES FOR TESTING)

        // Current time in ISO 8601 format to compare against
        val currentTime = LocalDateTime.now()

        // Go through the hourly weather data
        if (hourlyCodes != null && hourlyTimes != null) {
            for (i in hourlyCodes.indices) {
                // Convert each hourly timestamp into LocalDateTime for comparison
                val hourlyTime = LocalDateTime.parse(hourlyTimes[i], DateTimeFormatter.ISO_DATE_TIME)

                // Only consider future hours and non-duplicate timestamps of rain
                if (hourlyTime.isAfter(currentTime) && !hourlyTime.equals(lastRainForecastTimestamp)) {
                    // Check if the weather code corresponds to rain
                    if (hourlyCodes[i] in rainCodes) {
                        lastRainForecastTimestamp = hourlyTime // Update the last rain forecast timestamp
                        return Pair(hourlyCodes[i], convertToSimpleTime(hourlyTime.toString())) // Return the weather code and hour
                    }
                }
            }
        }

        return null // No rain found in future hours
    }

    // Helper function for converting ISO 8601 timestamp to a readable time

    fun convertToSimpleTime(isoTimestamp: String): String {
        // Parse the ISO 8601 timestamp to a LocalDateTime object
        val dateTime = LocalDateTime.parse(isoTimestamp, DateTimeFormatter.ISO_DATE_TIME)

        // Define a formatter to convert the time to a readable format like "3:00 PM"
        val formatter = DateTimeFormatter.ofPattern("h:mm a")

        // Format the LocalDateTime object to the desired time format
        return dateTime.format(formatter)
    }
}