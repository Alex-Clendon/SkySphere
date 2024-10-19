package com.skysphere.skysphere.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
   Entity class that holds data to be stored in the local database according to a key
 */
@Entity(tableName = "daily_weather")
data class DailyWeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val temperatureMax: Double?,
    val temperatureMin: Double?,
    val weatherCode: Int?,
    val precipitationProbability: Int?,
    val precipitationSum: Double?,
    val apparentTemperatureMax: Double?,
    val apparentTemperatureMin: Double?,
    val windSpeed: Double?,
    val windDegrees: Double?,
    val sunrise: String?,
    val sunset: String?,
    val sunshineDuration: Double?,
    val uvIndexMax: Double?,
    val visibility: Double?,
    val time: String?,
    val timestamp: Long
)