package com.skysphere.skysphere.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skysphere.skysphere.data.entities.CurrentWeatherEntity

/*
    DAO class to perform data queries on the local database
 */
@Dao
interface CurrentWeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(currentWeather: CurrentWeatherEntity)

    @Query("SELECT * FROM current_weather ORDER BY timestamp DESC LIMIT 1")
    fun getLatestCurrentWeather(): CurrentWeatherEntity?

    @Query("DELETE FROM current_weather")
    suspend fun clearCurrentWeather()
}
