package com.skysphere.skysphere.data
/*
    Management class that handles all preferences
 */
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.skysphere.skysphere.ui.settings.SettingsFragment.Companion.SEVERE_NOTIFICATION_PREFERENCE_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val appPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val locationPreferences =
        context.getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)

    fun setPreferredUnit(key: String?, unit: String) {
        val editor = appPreferences.edit()
        editor.putString(key, unit)
        editor.apply()
    }

    fun getTemperatureUnit(): String {
        return appPreferences.getString("temperature_unit", "Celsius") ?: "Celsius"
    }

    fun getTemperatureSymbol(): String {
        return if (getTemperatureUnit() == "Fahrenheit") "°F" else "°C"
    }

    fun getWindSpeedUnit(): String {
        return when (appPreferences.getString("wind_speed_unit", "")) {
            "mph" -> "mph"
            "km/h" -> "km/h"
            "m/s" -> "m/s"
            "knots" -> "knots"
            else -> "km/h"
        }
    }

    fun getPrecipitationUnit(): String {
        return appPreferences.getString("rainfall_unit", "mm") ?: "mm"
    }

    fun getVisibilityUnit(): String {
        return appPreferences.getString("visibility_unit", "km") ?: "mi."
    }

    fun getTtsStatus(): String {
        return appPreferences.getString("tts", "disabled") ?: "disabled"
    }

    fun getTtsString(): String {
        return if (getTtsStatus() == "disabled") "Disabled" else "Enabled"
    }

    fun getCustomLocation(): String? {
        return locationPreferences.getString("place_name", "Custom Location")
    }

    fun saveLocation(latitude: Double, longitude: Double, location: String?) {
        val editor = locationPreferences.edit()
        editor.putFloat("latitude", latitude.toFloat())
        editor.putFloat("longitude", longitude.toFloat())
        editor.putString("place_name", location)
        editor.apply()
    }

    // Saving the preference for the severe weather warnings notification of the user
    fun saveNotificationPreference(key: String, enabled: Boolean,) {
        val editor = appPreferences.edit()
        editor.putBoolean(key, enabled)
        editor.apply()
    }

    fun checkNotification(key: String, preference: Boolean): Boolean {
        return appPreferences.getBoolean(key, preference)
    }

    fun isFirstOpened(): Boolean {
        return appPreferences.getBoolean("isFirstOpen", false)
    }

    fun saveFirstOpened(boolean: Boolean) {
        val editor = appPreferences.edit()
        editor.putBoolean("isFirstOpen", boolean)
        editor.apply()
    }
}