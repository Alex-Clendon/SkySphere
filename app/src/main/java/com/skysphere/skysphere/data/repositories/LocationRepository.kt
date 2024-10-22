package com.skysphere.skysphere.data.repositories

import android.content.Context
import com.skysphere.skysphere.data.dao.locations.LocationDao
import com.skysphere.skysphere.data.entities.locations.LocationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/*
    Repository class to handle the database queries
 */
class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    @ApplicationContext private val context: Context
) {

    // Insert a location to the table
    suspend fun insertLocation(area: String, country: String, latitude: Double, longitude: Double) {

        // Check if a location with the same area already exists
        val existingLocation = locationDao.getLocationByName(area)

        if (existingLocation != null) {
            // If true, update the existing data
            val updatedLocationEntity = existingLocation.copy(
                country = country,
                latitude = latitude,
                longitude = longitude
            )
            locationDao.insertLocation(updatedLocationEntity)
        } else {
            // If it does not exist, insert a new location
            val locationEntity = LocationEntity(
                area = area,
                country = country,
                latitude = latitude,
                longitude = longitude
            )
            locationDao.insertLocation(locationEntity) // New entry
        }
    }


    // Fetch all locations
    suspend fun getAllLocations(): List<LocationEntity> {

        val list = locationDao.getAllLocations()
        // Returns a sorted list by id ascending order
        return list.sortedBy { if (it.id == 0) -1 else it.id }
    }

    // Delete a location
    suspend fun deleteLocation(area: String) {
        locationDao.deleteLocation(area)
    }

    suspend fun saveCurrentLocation(
        area: String?,
        country: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        // If parameters are not null, save the current location into the table at id of 1 so it is always replaced
        if (area != null && country != null && latitude != null && longitude != null) {
            val locationEntity = LocationEntity(
                id = 1,
                area = area,
                country = country,
                latitude = latitude,
                longitude = longitude
            )
            locationDao.insertLocation(locationEntity)
        }
    }

}
