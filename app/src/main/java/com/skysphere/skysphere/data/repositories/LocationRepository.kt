package com.skysphere.skysphere.data.repositories

import android.content.Context
import com.skysphere.skysphere.data.dao.locations.LocationDao
import com.skysphere.skysphere.data.entities.locations.LocationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    @ApplicationContext private val context: Context) {

    // Insert a location
    suspend fun insertLocation(location: LocationEntity) {
        locationDao.insertLocation(location)
    }

    // Fetch all locations
    suspend fun getAllLocations(): List<LocationEntity> {
        return locationDao.getAllLocations()
    }

    // Delete a location
    suspend fun deleteLocation(location: LocationEntity) {
        locationDao.deleteLocation(location)
    }
}
