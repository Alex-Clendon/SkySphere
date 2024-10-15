package com.skysphere.skysphere.updaters.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.services.weather.WeatherService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    private val api: WeatherService,
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val viewModel: WeatherViewModel
): CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        try {
            val response = api.getWeather()
            viewModel.setWeatherResults(response)

            val currentTime = System.currentTimeMillis()
            saveLastExecutionTime(currentTime)

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
