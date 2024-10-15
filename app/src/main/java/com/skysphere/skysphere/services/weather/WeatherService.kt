package com.skysphere.skysphere.services.weather

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.skysphere.skysphere.API.RetrofitInstance
import com.skysphere.skysphere.services.weather.json.ApiResults
import com.skysphere.skysphere.updaters.WeatherCache
import javax.inject.Inject
import retrofit2.await

class WeatherService @Inject constructor(
    private val weatherCache: WeatherCache
) {

    suspend fun getWeather(): ApiResults {
        /*if (weatherCache.isCacheValid()) {
        // Use cached data
        weatherCache.cachedWeatherResults?.let(onSuccess) ?: onFailure(Throwable("Cached data is null"))
        Log.d("Cache", "Cache Used")
        return
    }*/
        val daily = arrayOf(
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "apparent_temperature_max",
            "apparent_temperature_min",
            "precipitation_sum",
            "precipitation_probability_max",
            "sunrise",
            "sunset",
            "sunshine_duration",
            "uv_index_max"
        )

        val hourly = arrayOf(
            "temperature_2m",
            "apparent_temperature",
            "precipitation_probability",
            "precipitation",
            "weather_code",
            "is_day",
            "relative_humidity_2m",
        )

        val current = arrayOf(
            "temperature_2m",
            "apparent_temperature",
            "weather_code",
            "wind_speed_10m",
            "wind_direction_10m",
            "relative_humidity_2m",
            "visibility",
        )


        val api = RetrofitInstance.getInstance(true)
        return api.getWeatherData2(
            latitude = -36.85, // After testing, use location.latitude,
            longitude = 174.76, // After testing, use location.longitude,
            daily = daily.joinToString(","),
            hourly = hourly.joinToString(","),
            current = current.joinToString(","),
            timezone = "auto",
            forecastDays = 7
        ).await()
    }
}


