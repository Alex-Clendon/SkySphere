package com.skysphere.skysphere.data.dao.weather

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skysphere.skysphere.data.entities.weather.HourlyWeatherEntity

/*
    DAO class to perform data queries on the local database
 */
@Dao
interface HourlyWeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>)

    @Query("SELECT * FROM hourly_weather ORDER BY time ASC")
    fun getHourlyWeather(): List<HourlyWeatherEntity>?

    @Query("DELETE FROM hourly_weather")
    suspend fun clearHourlyWeather()
}

