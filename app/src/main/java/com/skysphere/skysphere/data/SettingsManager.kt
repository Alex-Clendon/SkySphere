package com.skysphere.skysphere.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val locationPreferences = context.getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)

    fun getTemperatureUnit(): String {
        return sharedPreferences.getString("temperature_unit", "Celsius") ?: "Celsius"
    }

    fun getTemperatureSymbol(): String {
        return if (getTemperatureUnit() == "Fahrenheit") "°F" else "°C"
    }

    fun getCustomLocation() : String? {
        return locationPreferences.getString("place_name", "Custom Location")
    }
}