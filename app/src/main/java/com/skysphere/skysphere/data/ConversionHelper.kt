package com.skysphere.skysphere.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.transition.Visibility
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object ConversionHelper {

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToDate(isoString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val dateTime = LocalDateTime.parse(isoString, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.getDefault())
        return dateTime.format(outputFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToDay(isoString: String?): String? {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        val dateTime = LocalDate.parse(isoString, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
        return dateTime.format(outputFormatter)
    }

    fun convertRoundedTemperature(temperature: Double?, unit: String): Int? {
        return temperature?.let {
            if (unit == "Fahrenheit") {
                ((it * 9 / 5) + 32).roundToInt()
            } else {
                it.roundToInt()
            }
        }
    }

    fun convertTemperature(temperature: Double?, unit: String): Double? {
        return temperature?.let {
            if (unit == "Fahrenheit") {
                ((it * 9 / 5) + 32)
            } else {
                it
            }
        }
    }

    fun convertWindSpeed(windSpeed: Double?, unit: String): Double? {
        if (unit == "mph") {
            return windSpeed?.div(1.609)
        } else if (unit == "m/s") {
            return windSpeed?.div(3.6)
        } else if (unit == "knots") {
            return windSpeed?.div(1.852)
        }
        return windSpeed
    }

    fun convertWindDirection(direction: Double?): String? {
        if (direction != null) {
            if (direction >= 0 && direction < 23) {
                return "N";
            } else if (direction >= 23 && direction < 68) {
                return "NE";
            } else if (direction >= 68 && direction < 113) {
                return "E";
            } else if (direction >= 113 && direction < 158) {
                return "SE";
            } else if (direction >= 158 && direction < 203) {
                return "S";
            } else if (direction >= 203 && direction < 248) {
                return "SW";
            } else if (direction >= 248 && direction < 293) {
                return "W";
            } else if (direction >= 293 && direction < 338) {
                return "NW";
            } else {
                return "N";
            }
        }
        return "(Unknown)";

    }

    fun convertUV(uvIndex: Double?): String? {
        if (uvIndex != null) {
            if (uvIndex >= 1 && uvIndex <= 2) {
                return "(Low)"
            } else if (uvIndex >= 3 && uvIndex <= 5) {
                return "(Moderate)"
            } else if (uvIndex >= 6 && uvIndex <= 7) {
                return "(High)"
            } else if (uvIndex >= 8 && uvIndex <= 10) {
                return "(Very High)"
            } else {
                return "(Extreme)"
            }
        }
        return "(Unknown)"
    }

    fun convertVisibility(visibility: Double?, unit: String): Double? {
        var visibilityKm = visibility?.div(1000)
        return visibility?.let {
            if (unit == "mi.") {
                return visibilityKm?.div(1.609)
            } else {
                return visibilityKm
            }
        }
    }
}