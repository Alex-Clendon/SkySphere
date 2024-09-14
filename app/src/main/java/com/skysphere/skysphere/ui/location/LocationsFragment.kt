package com.skysphere.skysphere.ui.location

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.skysphere.skysphere.R

class LocationsFragment : Fragment(), OnMapReadyCallback {

    // Initialize necessary variables
    private var mGoogleMap:GoogleMap? = null // Google map object from Google API
    private lateinit var autocompleteFragment:AutocompleteSupportFragment // Autocomplete object from Google API
    private lateinit var setLocationButton: Button
    private lateinit var selectedLatLng: LatLng
    private lateinit var selectedAddress: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations, container, false)

        Places.initialize(requireContext(), getString(R.string.google_map_api_key)) //Calling Google Places API
        autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) //Implementing autocomplete into the search field
                as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)) // Decide the variables returned on selection
        autocompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
            override fun onError(p0: Status) {
                // Error handling
            }

            override fun onPlaceSelected(place: Place) { // Set variables once place has been selected
                selectedLatLng = place.latLng
                selectedAddress = place.address
                zoomIn(selectedLatLng) // Zoom in to selected location
                setLocationButton.visibility = View.VISIBLE // Make button visible only when place has been selected
            }

        })

        setLocationButton = view.findViewById(R.id.btnSetLocation)

        setLocationButton.setOnClickListener {
            selectedLatLng?.let { latLng ->
                saveLocation(latLng, selectedAddress) // Save the chosen location to preferences
            }
        }


        val mapFragment = childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    private fun zoomIn(latLng: LatLng) //Function to zoom in on map upon selecting a location
    {
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng, 13f) // 13f -> zoom level
        mGoogleMap?.animateCamera(newLatLngZoom)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

    }

    // Use SharedPreferences to save location
    private fun saveLocation(latLng: LatLng, address: String?) {
        val sharedPrefs = requireContext().getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putFloat("latitude", latLng.latitude.toFloat())
            putFloat("longitude", latLng.longitude.toFloat())
            putString("place_name", address)
            apply() // Save data
        }

        Toast.makeText(requireContext(), "Location Updated", Toast.LENGTH_LONG).show()

        // Navigate back to the home fragment
        parentFragmentManager.popBackStack()
    }
}