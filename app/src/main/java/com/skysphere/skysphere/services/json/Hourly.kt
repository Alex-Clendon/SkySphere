package com.skysphere.skysphere.services.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class Hourly(
    val time: LongArray,
    @SerialName("temperature_2m") val temperature: Array<Double?>?,
    @SerialName("apparent_temperature") val apparentTemperature: Array<Double?>?,
    @SerialName("precipitation_probability") val precipitationProbability: Array<Int?>?,
    val precipitation: Array<Double?>?,
    @SerialName("weathercode") val weatherCode: Array<Int?>?,
    @SerialName("is_day") val isDay: IntArray?, // 0 = Night, 1 = Day
    val visibility: Array<Double?>?
)


