package com.skysphere.skysphere.data.dao.locations

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skysphere.skysphere.data.entities.locations.LocationEntity
import com.skysphere.skysphere.data.entities.weather.CurrentWeatherEntity

/*
    DAO class to perform data queries on the local database
 */
@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM locations WHERE area = :areaName LIMIT 1")
    suspend fun getLocationByName(areaName: String): LocationEntity?

    // Get all locations
    @Query("SELECT * FROM locations")
    suspend fun getAllLocations(): List<LocationEntity>

    // Delete a location by ID
    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    @Query("DELETE FROM locations WHERE area = :areaName")
    suspend fun deleteLocationByName(areaName: String)

}