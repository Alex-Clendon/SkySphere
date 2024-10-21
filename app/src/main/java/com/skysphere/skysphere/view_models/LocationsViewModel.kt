package com.skysphere.skysphere.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysphere.skysphere.data.entities.locations.LocationEntity
import com.skysphere.skysphere.data.repositories.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel@Inject constructor(
    private val locationRepository: LocationRepository
): ViewModel() {

    // Insert a new location
    fun insertLocation(location: LocationEntity) {
        viewModelScope.launch {
            locationRepository.insertLocation(location)
        }
    }

    // Fetch all locations
    fun fetchLocations(callback: (List<LocationEntity>) -> Unit) {
        viewModelScope.launch {
            val locations = locationRepository.getAllLocations()
            callback(locations)
        }
    }

    // Delete a location
    fun deleteLocation(location: LocationEntity) {
        viewModelScope.launch {
            locationRepository.deleteLocation(location)
        }
    }
}
