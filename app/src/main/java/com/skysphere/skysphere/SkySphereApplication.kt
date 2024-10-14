package com.skysphere.skysphere

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.skysphere.skysphere.services.weather.WeatherService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.skysphere.skysphere.updaters.background.WeatherUpdateWorker

@HiltAndroidApp
class SkySphereApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

}

class CustomWorkerFactory @Inject constructor(private val api: WeatherService): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = WeatherUpdateWorker(api, appContext, workerParameters)

}