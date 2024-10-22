package com.skysphere.skysphere

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.skysphere.skysphere.background.WeatherUpdateWorker
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.repositories.WeatherRepository
import com.skysphere.skysphere.notifications.WeatherCheckWorker
import com.skysphere.skysphere.view_models.WeatherViewModel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

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

class CustomWorkerFactory @Inject constructor(
    private val viewModel: WeatherViewModel,
    private val weatherRepo: WeatherRepository,
    private val settingsManager: SettingsManager
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            WeatherUpdateWorker::class.java.name -> WeatherUpdateWorker(appContext, workerParameters, weatherRepo, viewModel)
            WeatherCheckWorker::class.java.name -> WeatherCheckWorker(appContext, workerParameters, viewModel, settingsManager)
            else -> null
        }
    }
}