package com.skysphere.skysphere.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysphere.skysphere.data.entities.locations.LocationEntity
import com.skysphere.skysphere.data.repositories.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _locations = MutableLiveData<List<LocationEntity>>()
    val locations: LiveData<List<LocationEntity>> = _locations

    // Fetch all locations
    fun fetchLocations() {
        viewModelScope.launch {
            val locationsList = locationRepository.getAllLocations()
            _locations.postValue(locationsList) // Update LiveData
        }
    }
}

