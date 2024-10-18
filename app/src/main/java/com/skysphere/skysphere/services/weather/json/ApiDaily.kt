package com.skysphere.skysphere.services.weather.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class ApiDaily(
    val time: List<String?>?,
    @SerialName("weather_code") val weatherCode: List<Int?>?,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double?>?,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double?>?,
    @SerialName("precipitation_probability_max") val precipitationProbability: List<Int?>?,
    @SerialName("precipitation_sum") val precipitationSum: List<Double?>?,
    @SerialName("wind_speed_10m_max") val windSpeed: List<Double?>?,
    @SerialName("wind_direction_10m_dominant") val windDirection: List<Double?>?,
    @SerialName("apparent_temperature_max") val apparentTemperatureMax: List<Double?>?,
    @SerialName("apparent_temperature_min") val apparentTemperatureMin: List<Double?>?,
    val sunrise: List<String?>?,
    val sunset: List<String?>?,
    @SerialName("sunshine_duration") val sunshineDuration: List<Double?>?,
    @SerialName("uv_index_max") val uvIndexMax: List<Double?>?,
    @SerialName("visibility_min") val visibility: List<Double?>?
)


