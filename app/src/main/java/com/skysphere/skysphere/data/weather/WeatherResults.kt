package com.skysphere.skysphere.data.weather

// Class to hold weather results, combining hourly and daily data
data class WeatherResults(
    val current: WeatherCurrent?,
    val hourly: WeatherHourly?,
    val daily: WeatherDaily?
)

// Class to hold current weather data
data class WeatherCurrent(
    val temperature: Double?,
    val apparentTemperature: Double?,
    val relativeHumidity: Int?,
    val weatherCode: Int?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val visibility: Double?,
    val time: String
)

// Class to hold hourly weather
data class WeatherHourly(
    val time: List<String?>,
    val temperature: List<Double?>,
    val apparentTemperature: List<Double?>,
    val precipitationProbability: List<Int?>,
    val precipitation: List<Double?>,
    val weatherCode: List<Int?>,
    val isDay: List<Int?>,
)

// Class to hold daily weather data
data class WeatherDaily(
    val time: List<String?>,
    val weatherCode: List<Int?>,
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
