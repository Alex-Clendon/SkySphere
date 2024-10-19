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
import com.skysphere.skysphere.background.WeatherUpdateWorker
import com.skysphere.skysphere.data.WeatherRepository
/*
    Application class to configure Android Hilt 
 */
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

class CustomWorkerFactory @Inject constructor(private val viewModel: WeatherViewModel, private val weatherRepo: WeatherRepository): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = WeatherUpdateWorker(appContext, workerParameters, weatherRepo, viewModel)

}