package com.skysphere.skysphere.services.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class Daily(
    val time: LongArray,
    @SerialName("weather_code") val weatherCode: Array<Int?>?,
    @SerialName("temperature_2m_max") val temperatureMax: Array<Double?>?,
    @SerialName("temperature_2m_min") val temperatureMin: Array<Double?>?,
    @SerialName("precipitation_probability_max") val precipitationProbability: Array<Int?>?,
    @SerialName("apparent_temperature_max") val apparentTemperatureMax: Array<Double?>?,
    @SerialName("apparent_temperature_min") val apparentTemperatureMin: Array<Double?>?,
    val sunrise: Array<Long?>?,
    val sunset: Array<Long?>?,
    @SerialName("sunshine_duration") val sunshineDuration: Array<Double?>?,
    @SerialName("uv_index_max") val uvIndexMax: Array<Double?>?
)


