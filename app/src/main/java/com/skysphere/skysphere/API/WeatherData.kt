package com.skysphere.skysphere.API

data class WeatherData(
    val current: Current,
    val hourly: Hourly
)

data class Current(
    val weather_code: Int,
    val temperature_2m: Double
)

data class Hourly(
    val apparent_temperature: List<Double>
)
