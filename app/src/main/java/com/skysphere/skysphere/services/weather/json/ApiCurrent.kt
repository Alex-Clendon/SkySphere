package com.skysphere.skysphere.services.weather.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class ApiCurrent(
    @SerialName("temperature_2m") val temperature: Double?,  // Works as expected
    @SerialName("apparent_temperature") val apparentTemperature: Double?,
    @SerialName("relative_humidity_2m") val relativeHumidity: Int?,  // Matches JSON
    @SerialName("weather_code") val weatherCode: Int?,
    @SerialName("wind_speed_10m") val windSpeed: Double?,
    @SerialName("wind_direction_10m") val windDirection: Double?,
    val visibility: Double?,
    val time: String
)

