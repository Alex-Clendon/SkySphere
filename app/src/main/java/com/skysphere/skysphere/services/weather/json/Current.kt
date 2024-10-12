package com.skysphere.skysphere.services.weather.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class Current(
    @SerialName("temperature_2m") val temperature: Double?,
    @SerialName("apparent_temperature") val apparentTemperature: Double?,
    @SerialName("relative_humidity") val relativeHumidity: Double?,
    @SerialName("weather_code") val weatherCode: Int?,
    @SerialName("wind_speed_10m") val windSpeed: Double?,
    @SerialName("wind_direction_10m") val windDirection: Double?,
    @SerialName("uv_index") val uvIndex: Double?,
    val visibility: Double?,
    val time: String
)
