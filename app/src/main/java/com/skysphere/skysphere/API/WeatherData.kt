package com.skysphere.skysphere.API

data class WeatherData(
    val current: Current,
    val daily: Daily
)

data class Current(
    val weather_code: Int,
    val temperature_2m: Double
)

data class Daily(
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)
