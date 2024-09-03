package com.skysphere.skysphere

data class WeatherData(
    val hourly: Hourly
)

data class Hourly(
    val weather_code: List<Int>,
    val temperature_2m: List<Double>
)
