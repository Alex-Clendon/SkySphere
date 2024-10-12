package com.skysphere.skysphere.services.weather

import com.skysphere.skysphere.API.WeatherAPI
import com.skysphere.skysphere.services.weather.json.WeatherResults
import com.skysphere.skysphere.updaters.WeatherCache
import javax.inject.Inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherService @Inject constructor(
    private val api: WeatherAPI,
    private val weatherCache: WeatherCache
) {

    fun getWeather(
        // location: Location,
        onSuccess: (WeatherResults) -> Unit, // Change the type to WeatherResults
        onFailure: (Throwable) -> Unit
    ) {
        if (weatherCache.isCacheValid()) {
        // Use cached data
        weatherCache.cachedWeatherResults?.let(onSuccess) ?: onFailure(Throwable("Cached data is null"))
        return
    }
        val daily = arrayOf(
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "apparent_temperature_max",
            "apparent_temperature_min",
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
            "uv_index",
            "relative_humidity_2m",
            "visibility",
        )

        // Make sure the API method corresponds to the correct signature
        api.getWeatherData(
            latitude = -36.85, // After testing, use location.latitude,
            longitude = 174.76, // After testing, use location.longitude,
            daily = daily.joinToString(","),
            hourly = hourly.joinToString(","),
            current = current.joinToString(","),
            timezone = "auto",
            forecastDays = 7
        ).enqueue(object : Callback<WeatherResults> { // Change to WeatherResults
            override fun onResponse(call: Call<WeatherResults>, response: Response<WeatherResults>) {
                // Provide more detailed error messages based on response code if necessary
                if (response.isSuccessful) {
                    response.body()?.let {
                        weatherCache.updateCache(it) // Update the weather cache
                        onSuccess(it)
                    } ?: onFailure(Throwable("Response body is null"))
                } else {
                    onFailure(Throwable("Error fetching weather data: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<WeatherResults>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}


