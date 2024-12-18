package com.skysphere.skysphere.services.weather

import android.content.Context
import android.util.Log
import com.skysphere.skysphere.API.RetrofitInstance
import com.skysphere.skysphere.services.weather.json.ApiResults
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import retrofit2.await

/*
    Operative class that handles the API call
 */
class WeatherService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getWeather(): ApiResults {
        val location = context.getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
        val latitude = location.getFloat("latitude", 0f).toDouble()
        val longitude = location.getFloat("longitude", 0f).toDouble()

        /*
            Store requests into string arrays
         */

        val daily = arrayOf(
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "apparent_temperature_max",
            "apparent_temperature_min",
            "precipitation_sum",
            "precipitation_probability_max",
            "wind_speed_10m_max",
            "wind_direction_10m_dominant",
            "sunrise",
            "sunset",
            "daylight_duration",
            "uv_index_max",
            "visibility_min"
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
            "precipitation_probability",
            "precipitation",
            "relative_humidity_2m",
            "visibility",
        )

        /*
            Make the API call using the previous request arrays
         */
        val api = RetrofitInstance.getInstance(true)
        return api.getWeatherData(
            latitude,
            longitude,
            daily = daily.joinToString(","),
            hourly = hourly.joinToString(","),
            current = current.joinToString(","),
            timezone = "auto",
            forecastDays = 7
        ).await()
    }
}


