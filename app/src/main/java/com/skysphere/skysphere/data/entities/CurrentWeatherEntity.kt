package com.skysphere.skysphere.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather")
data class CurrentWeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val temperature: Double?,
    val apparentTemperature: Double?,
    val humidity: Int?,
    val weatherCode: Int?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val visibility: Double?,
    val time: String,
    val timestamp: Long
)