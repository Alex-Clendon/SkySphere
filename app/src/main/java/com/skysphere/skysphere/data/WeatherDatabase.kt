package com.skysphere.skysphere.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skysphere.skysphere.data.dao.CurrentWeatherDao
import com.skysphere.skysphere.data.dao.DailyWeatherDao
import com.skysphere.skysphere.data.dao.HourlyWeatherDao
import com.skysphere.skysphere.data.entities.CurrentWeatherEntity
import com.skysphere.skysphere.data.entities.HourlyWeatherEntity
import com.skysphere.skysphere.data.entities.DailyWeatherEntity

@Database(entities = [CurrentWeatherEntity::class, HourlyWeatherEntity::class, DailyWeatherEntity::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun currentWeatherDao(): CurrentWeatherDao
    abstract fun hourlyWeatherDao(): HourlyWeatherDao
    abstract fun dailyWeatherDao(): DailyWeatherDao
}