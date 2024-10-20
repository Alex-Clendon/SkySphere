package com.skysphere.skysphere.API

/*
    Seperate data class exclusively for the widget, as it has not been overhauled
 */
data class WeatherData(
    val current: Current

)

data class Current(
    val weather_code: Int,
    val temperature_2m: Double
)