package com.skysphere.skysphere.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skysphere.skysphere.data.entities.DailyWeatherEntity

@Dao
interface DailyWeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>)

    @Query("SELECT * FROM daily_weather ORDER BY time ASC")
    fun getDailyWeather(): List<DailyWeatherEntity>?

    @Query("DELETE FROM daily_weather")
    suspend fun clearDailyWeather()
}


