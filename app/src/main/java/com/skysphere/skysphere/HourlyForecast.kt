package com.skysphere.skysphere

import java.util.Date

data class HourlyForecast(
    val time: Date,
    val temperature: Int,
    val condition: String,
)
