package com.skysphere.skysphere

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class GPSManager(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    interface GPSManagerCallback {
        fun onLocationRetrieved(
            latitude: Double,
            longitude: Double,
            addressDetails: String?
        )
        fun onLocationError(error: String)
    }

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
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (addresses?.isNotEmpty() == true) {
                    val address = addresses[0]

                    // Extract address components just like in the LocationsFragment
                    val sublocality = address.subLocality // District
                    val locality = address.locality // City / Town
                    val adminArea = address.adminArea // State / Region
                    val country = address.countryName // Country

                        // String Falls back to the next option if the current one is unavailable.
                    val addressDetails = sublocality ?: locality ?: adminArea ?: country ?: "Unknown Location"
                    val sharedPrefs = context.getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
                    with(sharedPrefs.edit()) {
                        putFloat("latitude", location.latitude.toFloat())
                        putFloat("longitude", location.longitude.toFloat())
                        putString("place_name", addressDetails)
                        apply()
                    }
                    callback.onLocationRetrieved(location.latitude, location.longitude, addressDetails)
                }
                else
                {
                    callback.onLocationError("Unknown Location")
                }
            }
            else {
                callback.onLocationError("Unknown Location")
            }
        }.addOnFailureListener { e ->
            callback.onLocationError(e.message ?: "Error retrieving location.")
        }
    }
}
