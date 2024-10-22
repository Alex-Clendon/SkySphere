package com.skysphere.skysphere.ui.location

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.skysphere.skysphere.R
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.repositories.LocationRepository
import com.skysphere.skysphere.view_models.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class LocationsMapFragment : Fragment(), OnMapReadyCallback {

    // Inject necessary variables
    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var viewModel: WeatherViewModel

    @Inject
    lateinit var settingsManager: SettingsManager

    private var mGoogleMap: GoogleMap? = null // Google map object from Google API
    private lateinit var autocompleteFragment: AutocompleteSupportFragment // Autocomplete object from Google API
    private lateinit var setLocationButton: Button
    private lateinit var selectedLatLng: LatLng
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var area: String = ""
    private var country: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations_map, container, false)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle("")

        Places.initialize(
            requireContext(),
            getString(R.string.google_map_api_key)
        ) //Calling Google Places API
        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) //Implementing autocomplete into the search field
                    as AutocompleteSupportFragment
        autocompleteFragment.setHint("Search")
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG
            )
        ) // Decide the variables returned on selection
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                // Error handling
            }

            override fun onPlaceSelected(place: Place) {

                selectedLatLng = place.latLng
                // Retrieve locations latitude and longitude
                latitude = selectedLatLng.latitude ?: 0.0
                longitude = selectedLatLng.longitude ?: 0.0

                // Extract address components from API response
                val addressComponents = place.addressComponents?.asList() ?: listOf()
                country = addressComponents.firstOrNull {
                    it.types.contains("country")
                }?.name ?: "Unknown Country"

                // Search for the most specific location, starting with sublocality
                area = addressComponents.firstOrNull {
                    it.types.contains("sublocality")
                }?.name
                        // Fallback to locality if sublocality is not available
                    ?: addressComponents.firstOrNull {
                        it.types.contains("locality")
                    }?.name
                            // Fallback to administrative area (the broader city or region) if neither sublocality nor locality are available
                            ?: addressComponents.firstOrNull {
                        it.types.contains("administrative_area_level_1")
                    }?.name
                            // Fallback to country if none of the above are available
                            ?: addressComponents.firstOrNull {
                        it.types.contains("country")
                    }?.name
                            // Fallback to a custom string if absolutely nothing is available
                            ?: "Unknown Location"
                zoomIn(selectedLatLng)
                setLocationButton.visibility = View.VISIBLE
            }
        })

        setLocationButton = view.findViewById(R.id.btnSetLocation)

        setLocationButton.setOnClickListener {
            selectedLatLng?.let { latLng ->
                // Insert the location into the database using a coroutine
                viewLifecycleOwner.lifecycleScope.launch {
                    locationRepository.insertLocation(area, country, latitude, longitude)
                    // Provide a small delay so table has time to update
                    delay(100)
                    // Swap back to the locations list fragment
                    val navController = findNavController()
                    val navOptions = navOptions {
                        popUpTo(R.id.nav_locations) { inclusive = true }
                    }
                    navController.navigate(R.id.action_nav_locations, null, navOptions)

                }

            }
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    private fun zoomIn(latLng: LatLng) //Function to zoom in on map upon selecting a location
    {
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng, 13f) // 13f -> zoom level
        mGoogleMap?.animateCamera(newLatLngZoom)
    }

    private fun zoomCurrent(latitude: Double, longitude: Double) //Function to zoom in on map upon fragment start
    {
        val latLng = LatLng(latitude, longitude)
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng, 11f) // 13f -> zoom level
        mGoogleMap?.animateCamera(newLatLngZoom)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        val location = requireContext().getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
        val currentLatitude = location.getFloat("latitude", 0f).toDouble()
        val currentLongitude = location.getFloat("longitude", 0f).toDouble()

        zoomCurrent(currentLatitude, currentLongitude)

    }
}