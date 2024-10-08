package com.skysphere.skysphere.API

data class WeatherData(
    val current: Current,
    val daily: Daily,
    val hourly: Hourly

)

data class Current(
    val time: String,
    val weather_code: Int,
    val temperature_2m: Double,
    val apparent_temperature: Double,
    val relative_humidity_2m: Int,
    val precipitation: Int,
    val wind_speed_10m: Double,
    val wind_direction_10m: Int
)

data class Daily(
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val time: List<String>,
    val visibility: List<Int>,
    val sunrise: List<String>,
    val sunset: List<String>,
    val daylight_duration: List<Double>,
    val uv_index_max: List<Double>
)

data class Hourly(
    val wind_speed_10m: List<Double>,
    val wind_direction_10m: List<Double>,
    val wind_gusts_10m: List<Double>,
    val temperature_2m: List<Double>,
)