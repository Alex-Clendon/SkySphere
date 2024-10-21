package com.skysphere.skysphere.data.databases.weather

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skysphere.skysphere.data.dao.weather.CurrentWeatherDao
import com.skysphere.skysphere.data.dao.weather.DailyWeatherDao
import com.skysphere.skysphere.data.dao.weather.HourlyWeatherDao
import com.skysphere.skysphere.data.entities.weather.CurrentWeatherEntity
import com.skysphere.skysphere.data.entities.weather.HourlyWeatherEntity
import com.skysphere.skysphere.data.entities.weather.DailyWeatherEntity
/*
    Database class that creates a Room local database using the previously set up entities
 */
@Database(entities = [CurrentWeatherEntity::class, HourlyWeatherEntity::class, DailyWeatherEntity::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun currentWeatherDao(): CurrentWeatherDao
    abstract fun hourlyWeatherDao(): HourlyWeatherDao
    abstract fun dailyWeatherDao(): DailyWeatherDao
}