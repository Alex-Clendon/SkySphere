package com.skysphere.skysphere.data.dao.weather

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skysphere.skysphere.data.entities.weather.DailyWeatherEntity

/*
    DAO class to perform data queries on the local database
 */
@Dao
interface DailyWeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>)

    @Query("SELECT * FROM daily_weather ORDER BY time ASC")
    fun getDailyWeather(): List<DailyWeatherEntity>?

    @Query("DELETE FROM daily_weather")
    suspend fun clearDailyWeather()
}


