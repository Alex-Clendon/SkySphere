package com.skysphere.skysphere.services.weather.json
import kotlinx.serialization.Serializable


@Serializable
data class ApiResults(
    val current: ApiCurrent? = null,
    val daily: ApiDaily? = null,
    val hourly: Hourly? = null
)


