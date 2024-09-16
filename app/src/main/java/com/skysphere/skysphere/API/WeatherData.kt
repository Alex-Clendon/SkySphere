package com.skysphere.skysphere.API

data class WeatherData(
    val current: Current,
    val daily: Daily,
    val hourly: Hourly

)

data class Current(
    val weather_code: Int,
    val temperature_2m: Double,
    val apparent_temperature: Double
)

data class Daily(
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val time: List<String>
)

data class Hourly(
    val wind_speed_10m: List<Double>,
    val wind_direction_10m: List<Double>,
    val wind_gusts_10m: List<Double>,
    val temperature_2m: List<Double>,
)