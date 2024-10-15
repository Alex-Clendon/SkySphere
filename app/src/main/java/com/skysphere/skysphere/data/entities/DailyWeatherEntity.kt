package com.skysphere.skysphere.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_weather")
data class DailyWeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val temperatureMax: List<Double?>?,
    val temperatureMin: List<Double?>?,
    val weatherCode: List<Int?>?,
    val apparentTemperature: List<Double?>?,
    val precipitationProbability: List<Int?>?,
    val precipitationSum: List<Double?>?,
    val apparentTemperatureMax: List<Double?>?,
    val apparentTemperatureMin: List<Double?>?,
    val sunrise: List<String?>?,
    val sunset: List<String?>?,
    val sunshineDuration: List<Double?>?,
    val uvIndexMax: List<Double?>?,
    val time: List<String?>?,
    val timestamp: Long
)