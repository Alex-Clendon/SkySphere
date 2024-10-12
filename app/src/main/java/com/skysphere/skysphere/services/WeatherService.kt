package com.skysphere.skysphere.services

import com.skysphere.skysphere.API.WeatherAPI
import com.skysphere.skysphere.API.WeatherData
import javax.inject.Inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherService @Inject constructor(
    private val api: WeatherAPI
) {

    fun getWeather(
        //location: Location,
        onSuccess: (WeatherData) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val daily = arrayOf(
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "apparent_temperature_max",
            "apparent_temperature_min",
            "time",
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
            "rain",
            "showers",
            "snowfall",
            "weathercode",
            "windspeed_10m",
            "winddirection_10m",
            "windgusts_10m",
            "uv_index",
            "is_day",
            "relativehumidity_2m",
            "dewpoint_2m",
            "pressure_msl",
            "cloudcover",
            "visibility"
        )

        val current = arrayOf(
            "temperature_2m",
            "apparent_temperature",
            "weathercode",
            "windspeed_10m",
            "winddirection_10m",
            "windgusts_10m",
            "uv_index",
            "relativehumidity_2m",
            "dewpoint_2m",
            "pressure_msl",
            "cloudcover",
            "visibility"
        )

        // Make sure the API method corresponds to the correct signature
        api.getWeatherData(
            latitude = -36.85, //After testing, use location.latitude,
            longitude = 174.76, //After testing, use location.longitude,
            daily = daily.joinToString(","),
            hourly = hourly.joinToString(","),
            current = current.joinToString(","),
            timezone = "auto",
            forecastDays = 7
        ).enqueue(object : Callback<WeatherData> {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                // Provide more detailed error messages based on response code if necessary
                if (response.isSuccessful) {
                    response.body()?.let(onSuccess) ?: onFailure(Throwable("Response body is null"))
                } else {
                    onFailure(Throwable("Error fetching weather data: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}

