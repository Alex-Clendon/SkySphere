package com.skysphere.skysphere

import java.util.Date

data class WeatherData(
    val location: String,
    val date: Date,
    val temperatureLow: Int,
    val temperatureHigh: Int,
    val feelsLikeTemperature: Int,
    val condition: String,
    val humidity: Int,
    val uvIndex: String,
    val windSpeed: Int,
    val sunrise: Date,
    val sunset: Date,
    val hourlyForecast: List<HourlyForecast>
)
