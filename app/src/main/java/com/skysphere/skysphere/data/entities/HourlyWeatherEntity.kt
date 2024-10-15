package com.skysphere.skysphere.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hourly_weather")
data class HourlyWeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val temperature: Double?,
    val apparentTemperature: Double?,
    val precipitationProbability: Int?,
    val precipitation: Double?,
    val weatherCode: Int?,
    val isDay: Int?,
    val time: String?,
    val timestamp: Long
)