package com.skysphere.skysphere.ui.home.models

class DailyModel(
    val day: String,         // Day of the week (e.g., "Today", "Monday")
    val weatherImage: Int,   // Resource ID for the weather image
    val tempMax: Int,        // Maximum temperature for the day
    val tempMin: Int         // Minimum temperature for the day
)
