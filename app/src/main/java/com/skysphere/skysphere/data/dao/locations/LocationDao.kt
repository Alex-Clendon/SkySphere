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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentLocation(
        area: String,
        country: String,
        latitude: Double,
        longitude: Double
    ) {
        val currentLocation = LocationEntity(
            id = 0, // Use id = 0 for the current location
            area = area,
            country = country,
            latitude = latitude,
            longitude = longitude
        )
        insertLocation(currentLocation)
    }

    // Get all locations
    @Query("SELECT * FROM locations")
    suspend fun getAllLocations(): List<LocationEntity>

    // Delete a location by ID
    @Delete
    suspend fun deleteLocation(location: LocationEntity)
}
