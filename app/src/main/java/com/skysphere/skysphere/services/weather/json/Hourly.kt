package com.skysphere.skysphere.services.weather.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class Hourly(
    val time: List<String?>?,
    @SerialName("temperature_2m") val temperature: List<Double?>?,
    @SerialName("apparent_temperature") val apparentTemperature: List<Double?>?,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?>?,
    val precipitation: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int?>?,
    @SerialName("is_day") val isDay: List<Int?>?, // 0 = Night, 1 = Day
)


