package com.skysphere.skysphere.services.weather.json
import kotlinx.serialization.Serializable

/*
    Data class that stores API response
    NOTE: This is NOT the data used in the UI. This is the data that gets parsed from the JSON response file
 */
@Serializable
data class ApiResults(
    val current: ApiCurrent? = null,
    val daily: ApiDaily? = null,
    val hourly: Hourly? = null
)


