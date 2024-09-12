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
    val wind_speed_10m: List<Double>,
    val wind_direction_10m: List<Double>,
    val wind_gusts_10m: List<Double>,
    val temperature_2m: List<Double>


)