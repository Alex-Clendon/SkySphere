package com.skysphere.skysphere.ui.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private var mGoogleMap:GoogleMap? = null
    private lateinit var autocompleteFragment:AutocompleteSupportFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations, container, false)

        Places.initialize(requireContext(), getString(R.string.google_map_api_key)) //Calling Google Places API
        autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) //Implementing autocomplete into the search field
                as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
            override fun onError(p0: Status) {
                Toast.makeText(requireContext(), "Search Error", Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {
                val add = place.address //Values returned once place is selected
                val id = place.id
                val latLng = place.latLng

            }

        })


        val mapFragment = childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

    }
}