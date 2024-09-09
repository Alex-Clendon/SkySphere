package com.skysphere.skysphere

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class GPSManager(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Callback interface to communicate the location data back to the fragment or activity.
    interface GPSManagerCallback {
        fun onLocationRetrieved(latitude: Double, longitude: Double, locality: String?)
        fun onLocationError(error: String)
    }

    // Get the current location of the user, checking for necessary permissions.
    fun getCurrentLocation(callback: GPSManagerCallback) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback.onLocationError("Location permissions are not granted.")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Use Geocoder to get the locality (city/town name).
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                val locality = if (addresses?.isNotEmpty() == true) addresses[0].locality else null
                callback.onLocationRetrieved(location.latitude, location.longitude, locality)
            } else {
                callback.onLocationError("Location Not Available.")
            }
        }.addOnFailureListener { e ->
            callback.onLocationError(e.message ?: "Error retrieving location.")
        }
    }
}