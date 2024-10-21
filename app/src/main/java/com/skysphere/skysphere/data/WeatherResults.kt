package com.skysphere.skysphere.data

import com.skysphere.skysphere.API.WeatherType

/*
   Data Class that holds all the weather data used by the app
 */
data class WeatherResults(
    val current: WeatherCurrent?,
    val hourly: WeatherHourly?,
    val daily: WeatherDaily?
)

// Class to hold current weather data
data class WeatherCurrent(
    val temperature: Double?,
    val apparentTemperature: Double?,
    val tempUnit: String,

    // Formatted Temperatures
    val roundedTemperature: Int?,
    val roundedApparentTemperature: Int?,

    val relativeHumidity: Int?,
    val weatherCode: Int?,
    val weatherType: WeatherType,
    val weatherText: String?,
    val windSpeed: Double?,
    val windSpeedUnit: String?,
    val windDegrees: Int?,
    val windDirection: String?,
    val precipitationProbability: Int?,
    val precipitation: Int?,
    val precipitationUnit: String?,
    val visibility: Double?,
    val visibilityUnit: String?,
    val time: String,
    val date: String,
    val updatedTime: String?
)

// Class to hold hourly weather data
data class WeatherHourly(
    val time: List<String?>,
    val temperature: List<Double?>,
    val apparentTemperature: List<Double?>,
    val precipitationProbability: List<Int?>,
    val precipitation: List<Double?>,
    val weatherCode: List<Int?>,
    val weatherText: List<String?>,
    val isDay: List<Int?>
)

// Class to hold daily weather data
data class WeatherDaily(
    val time: List<String?>,
    val weatherCode: List<Int?>,
    val weatherText: List<String?>,
    val weatherType: List<WeatherType?>,
    val temperatureMax: List<Double?>,
    val temperatureMin: List<Double?>,

    //Formatted temperatures
    val roundedTemperatureMax: List<Int?>,
    val roundedTemperatureMin: List<Int?>,

    val precipitationProbability: List<Int?>,
    val precipitationSum: List<Double?>,
    val windSpeed: List<Double?>,
    val windDegrees: List<Int?>,
    val windDirection: List<String?>,
    val apparentTemperatureMax: List<Int?>,
    val apparentTemperatureMin: List<Int?>,
    val sunrise: List<String?>,
    val sunset: List<String?>,
    val sunshineDuration: List<String?>,
    val uvIndex: List<Int?>,
    val uvIndexText: List<String?>,
    val visibility: List<Double?>,
    val day:  List<String?>
)
