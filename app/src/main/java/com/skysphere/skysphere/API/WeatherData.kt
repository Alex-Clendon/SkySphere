package com.skysphere.skysphere.API

data class WeatherData(
    val current: Current
)

data class Current(
    val weather_code: Int,
    val temperature_2m: Double
)
