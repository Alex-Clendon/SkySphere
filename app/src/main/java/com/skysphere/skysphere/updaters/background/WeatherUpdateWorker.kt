package com.skysphere.skysphere.updaters.background

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.WeatherRepository
import com.skysphere.skysphere.services.weather.WeatherService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
@RequiresApi(Build.VERSION_CODES.O)
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: WeatherRepository,
    private val viewModel: WeatherViewModel
): CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        try {
            repository.fetchAndStoreWeatherData()

            val currentTime = System.currentTimeMillis()
            saveLastExecutionTime(currentTime)
            viewModel.fetchWeatherData()
            Log.d("WeatherWorker", "Success, ${currentTime}")
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
