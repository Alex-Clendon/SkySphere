package com.skysphere.skysphere.data.repositories

import android.content.Context
import android.util.Log
import com.skysphere.skysphere.data.dao.locations.LocationDao
import com.skysphere.skysphere.data.entities.locations.LocationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    @ApplicationContext private val context: Context) {

    // Insert a location
    suspend fun insertLocation(area: String, country: String, latitude: Double, longitude: Double) {
        // Check if a location with the same area already exists
        val existingLocation = locationDao.getLocationByName(area)

        if (existingLocation != null) {
            // If it exists, you can update the existing location
            val updatedLocationEntity = existingLocation.copy(
                country = country,
                latitude = latitude,
                longitude = longitude
            )
            locationDao.insertLocation(updatedLocationEntity) // This will replace the existing entry
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
        Log.d("LOCATIONDEBUG", "${list}")
        return list.sortedBy { if (it.id == 0) -1 else it.id }
    }

    // Delete a location
    suspend fun deleteLocation(location: LocationEntity) {
        locationDao.deleteLocation(location)
    }

    suspend fun deleteLocationByArea(area: String) {
        locationDao.deleteLocationByName(area)
    }

    suspend fun saveCurrentLocation(area: String, country: String, latitude: Double, longitude: Double) {
        locationDao.insertCurrentLocation(
            area = area,
            country = country,
            latitude = latitude,
            longitude = longitude
        )
    }
}
