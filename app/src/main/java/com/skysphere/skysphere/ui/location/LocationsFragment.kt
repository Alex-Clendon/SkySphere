package com.skysphere.skysphere.ui.location

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.skysphere.skysphere.ui.home.HomePageFragment
import com.skysphere.skysphere.widgets.SkySphereWidget

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
        autocompleteFragment.setHint("Search")
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)) // Decide the variables returned on selection
        autocompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
            override fun onError(p0: Status) {
                // Error handling
            }

            override fun onPlaceSelected(place: Place) {
                selectedLatLng = place.latLng

                // Extract address components from API response
                val addressComponents = place.addressComponents?.asList() ?: listOf()

                // Search for the most specific location, starting with sublocality
                selectedAddress = addressComponents.firstOrNull {
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
                saveLocation(latLng, selectedAddress) // Save the chosen location to preferences
                updateWidget()
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
        // Set action bar title to Home
        val homeFragment = HomePageFragment()
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)
        (activity as AppCompatActivity?)!!.supportActionBar!!.title =
            "Home"
        // Swap fragment to home fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_main, homeFragment)
            .addToBackStack(null)
            .commit()
    }

    // This function will update the widget when the location is changed
    private fun updateWidget(){
        val applicationContext = requireContext().applicationContext

        val intent = Intent(requireContext(), SkySphereWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(ComponentName(applicationContext, SkySphereWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        requireContext().sendBroadcast(intent)
    }
}