package com.skysphere.skysphere

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class GPSManager(private val context: Context){
    private var fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var currentLocation: Location? = null // Store current location

    private val locationRequest: LocationRequest = LocationRequest.create().apply{ // Sets parameters for location updates
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Setting the priority to high accuracy
    }

    private val locationCallback = object : LocationCallback() { // Defines what happens when a location update is received
        override fun onLocationResult(locationResult: LocationResult){
            currentLocation = locationResult.lastLocation // Update the current location
        }
    }

    @SuppressLint("MissingPermission") // Permission is determined by the caller
    fun startLocationUpdates() { // Start receiving location updates based on locationRequest parameters
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun stopLocationUpdates() { // Stop receiving location updates and remove the location callback
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    fun getCurrentLocation(): Location? { // Return the most recent location from currentLocation.
        return currentLocation
    }

}