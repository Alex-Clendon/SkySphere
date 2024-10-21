package com.skysphere.skysphere.background

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skysphere.skysphere.view_models.WeatherViewModel
import com.skysphere.skysphere.data.repositories.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/*
    Worker class that automatically refreshes weather data by calling the API
 */
@HiltWorker
@RequiresApi(Build.VERSION_CODES.O)
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: WeatherRepository,
    private val viewModel: WeatherViewModel
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        try {
            // Call API and store data into local database
            repository.fetchAndStoreWeatherData()

            // Save fetch time
            val currentTime = System.currentTimeMillis()
            saveLastExecutionTime(currentTime)

            // Update shared view model
            viewModel.fetchWeatherData()
            return Result.success()
        } catch (e: Exception) {
            Log.d("WeatherWorker", "Error")
            return Result.retry()
        }
    }

    private fun saveLastExecutionTime(currentTime: Long) {
        val sharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong("last_execution_time", currentTime).apply()
    }
}
