package com.skysphere.skysphere.data

import android.os.Build
import androidx.annotation.RequiresApi
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
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val dateTime = LocalDateTime.parse(isoString, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
        return dateTime.format(outputFormatter)
    }

    fun convertTemperature(temperature: Double?, unit: String): Int? {
        return temperature?.let {
            if (unit == "Fahrenheit") {
                ((it * 9 / 5) + 32).roundToInt()
            } else {
                it.roundToInt()
            }
        }
    }
}