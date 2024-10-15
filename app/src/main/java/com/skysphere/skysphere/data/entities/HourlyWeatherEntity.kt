package com.skysphere.skysphere.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hourly_weather")
data class HourlyWeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val temperature: List<Double?>?,
    val apparentTemperature: List<Double?>?,
    val precipitationProbability: List<Int?>?,
    val precipitation: List<Double?>?,
    val weatherCode: List<Int?>?,
    val is_day: List<Int?>?,
    val time: List<String?>?,
    val timestamp: Long
)