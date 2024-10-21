package com.skysphere.skysphere.ui.location

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import com.skysphere.skysphere.view_models.WeatherViewModel
import com.skysphere.skysphere.background.WeatherUpdateWorker
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.repositories.WeatherRepository
import com.skysphere.skysphere.widgets.SkySphereWidget
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class LocationsMapFragment : Fragment(), OnMapReadyCallback {

    // Initialize necessary variables
    @Inject
    lateinit var repository: WeatherRepository

    @Inject
    lateinit var viewModel: WeatherViewModel

    @Inject
    lateinit var settingsManager: SettingsManager

    private var mGoogleMap: GoogleMap? = null // Google map object from Google API
    private lateinit var autocompleteFragment: AutocompleteSupportFragment // Autocomplete object from Google API
    private lateinit var setLocationButton: Button
    private lateinit var selectedLatLng: LatLng
    private lateinit var selectedAddress: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations_map, container, false)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_end)

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
                // Updated device preferences using SettingsManager
                settingsManager.saveLocation(selectedLatLng, selectedAddress)
                // Initiate a Coroutine to store API data in a database
                val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                    repeatInterval = 90,    // Set worker interval to 90 minutes
                    repeatIntervalTimeUnit = TimeUnit.MINUTES,
                ).setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.LINEAR,
                    duration = Duration.ofMinutes(15) // Retry in 15 minutes if needed
                )
                    .build()

                val workManager = WorkManager.getInstance(requireContext())

                workManager.enqueueUniquePeriodicWork(
                    "WeatherUpdateWork",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
                Toast.makeText(requireContext(), "Location Updated", Toast.LENGTH_SHORT).show()
                updateWidget()
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

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

    }

    // This function will update the widget when the location is changed
    private fun updateWidget() {
        val applicationContext = requireContext().applicationContext

        val intent = Intent(requireContext(), SkySphereWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(ComponentName(applicationContext, SkySphereWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        requireContext().sendBroadcast(intent)
    }
}