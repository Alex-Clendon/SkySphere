package com.skysphere.skysphere.data.weather

import androidx.annotation.DrawableRes
import com.skysphere.skysphere.API.WeatherType

// Class to hold weather results, combining hourly and daily data
data class WeatherResults(
    val current: WeatherCurrent?,
    val hourly: WeatherHourly?,
    val daily: WeatherDaily?
)

// Class to hold current weather data
data class WeatherCurrent(
    val temperature: Int?,
    val apparentTemperature: Int?,
    val tempUnit: String,
    val relativeHumidity: Int?,
    val weatherCode: Int?,
    val weatherType: WeatherType,
    val weatherText: String?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val visibility: Double?,
    val time: String,
    val date: String,
)

// Class to hold hourly weather
data class WeatherHourly(
    val time: List<String?>,
    val temperature: List<Double?>,
    val apparentTemperature: List<Double?>,
    val precipitationProbability: List<Int?>,
    val precipitation: List<Double?>,
    val weatherCode: List<Int?>,
    val weatherText: List<String?>,
    val isDay: List<Int?>,
)

// Class to hold daily weather data
data class WeatherDaily(
    val time: List<String?>,
    val weatherCode: List<Int?>,
    val weatherText: List<String?>,
    val temperatureMax: List<Double?>,
    val temperatureMin: List<Double?>,
    val precipitationProbability: List<Int?>,
    val precipitationSum: List<Double?>,
    val apparentTemperatureMax: List<Double?>,
    val apparentTemperatureMin: List<Double?>,
    val sunrise: List<String?>,
    val sunset: List<String?>,
    val sunshineDuration: List<Double?>,
    val uvIndexMax: List<Double?>
)
