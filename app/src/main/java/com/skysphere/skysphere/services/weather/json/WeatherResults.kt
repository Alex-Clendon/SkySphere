package com.skysphere.skysphere.services.weather.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class WeatherResults(
    val current: Current? = null,
    val daily: Daily? = null,
    val hourly: Hourly? = null,
    val error: Boolean? = null,
    val reason: String? = null
)

