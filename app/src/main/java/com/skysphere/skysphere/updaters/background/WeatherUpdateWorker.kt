package com.skysphere.skysphere.updaters.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.skysphere.skysphere.services.weather.WeatherService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    private val api: WeatherService,
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        try {
            val response = api.getWeather()
            Log.d("WeatherWorker", "Success")
            return Result.success()
        } catch (e: Exception) {
            Log.d("WeatherWorker", "Error")
            return Result.failure(Data.Builder().putString("Error", e.toString()).build())
        }
    }
}
