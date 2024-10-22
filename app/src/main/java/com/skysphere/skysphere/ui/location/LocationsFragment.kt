package com.skysphere.skysphere.ui.location

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.skysphere.skysphere.GPSManager
import com.skysphere.skysphere.R
import com.skysphere.skysphere.background.WeatherUpdateWorker
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.repositories.LocationRepository
import com.skysphere.skysphere.databinding.FragmentLocationsBinding
import com.skysphere.skysphere.ui.adapters.LocationsAdapter
import com.skysphere.skysphere.view_models.LocationViewModel
import com.skysphere.skysphere.widgets.SkySphereWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class LocationsFragment : Fragment(), GPSManager.GPSManagerCallback {

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationsAdapter: LocationsAdapter

    // Use Hilt to inject data
    @Inject
    lateinit var locationViewModel: LocationViewModel

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var settingsManager: SettingsManager

    private lateinit var gpsManager: GPSManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        gpsManager = GPSManager(requireContext())

        // Set the UI colours to white to match the background
        updateColours()

        // Initialize the RecyclerView and Adapter
        locationsAdapter = LocationsAdapter { location ->
            // Check if there is an internet connection
            val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            val isConnected = activeNetwork?.isConnectedOrConnecting == true

            if (!isConnected) {
                // Show a Snackbar if there is no internet connection
                Snackbar.make(binding.root, "Network Unavailable", Snackbar.LENGTH_SHORT).show()
            } else {
                // Save location to shared preferences to update the API call
                settingsManager.saveLocation(location.latitude, location.longitude, location.area)

                // Enqueue a new worker to make a new API call and store it
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

                Snackbar.make(binding.root, "Location Updated", Snackbar.LENGTH_SHORT).show()
                updateWidget()
            }
        }


        binding.locationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = locationsAdapter
        }

        // Create an itemTouchHelper to handle swiping gesture on card
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Logic for when card is swoped
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val location = locationsAdapter.getLocationAt(position)
                lifecycleScope.launch {
                    val locations = locationRepository.getAllLocations() // Fetch current list of locations

                    // If the current location is being used to display the data
                    if (settingsManager.getCustomLocation() == location.area) {
                        // Delete the swiped location (current location wont delete because of the delete function handling)
                        locationRepository.deleteLocation(location.area)
                        // Get the remaining location
                        val remainingLocation = locations.firstOrNull { it.id != location.id }
                        remainingLocation?.let {
                            // Save the remaining location and make a fresh API call for it
                            settingsManager.saveLocation(it.latitude, it.longitude, it.area)
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

                        }
                        // Refresh the list of locations
                        locationViewModel.fetchLocations()
                    } else {
                        // Otherwise, delete the location normally
                        locationRepository.deleteLocation(location.area)
                        locationViewModel.fetchLocations()
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.locationsRecyclerView)

        // If current location is null, show button to add it to cards. If not, show the regular add button
        locationViewModel.locations.observe(viewLifecycleOwner) { locations ->
            locationsAdapter.updateLocations(locations)
            if (locations.isEmpty()) {
                binding.addCurrentCard.visibility = View.VISIBLE
            } else {
                binding.addCurrentCard.visibility = View.GONE
                binding.addLocationCard.visibility = View.VISIBLE
            }
        }

        // Fetch locations when the fragment is created
        locationViewModel.fetchLocations()

        binding.addCurrentCard.setOnClickListener {
            gpsManager.getCurrentLocation(this)
        }

        // Navigation to map fragment when add button is pressed
        val navController = findNavController()
        binding.addLocationCard.setOnClickListener {
            navController.navigate(R.id.action_nav_map)
        }

        return binding.root
    }

    // Function to update the UI Colours
    private fun updateColours() {
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.background_white)
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.background_white) // Status Bar Color
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background_white
                )
            )
        )
        // Change default app colours
        actionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle("")
    }

    private fun updateWidget() {
        val applicationContext = requireContext().applicationContext

        val intent = Intent(requireContext(), SkySphereWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(ComponentName(applicationContext, SkySphereWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        requireContext().sendBroadcast(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Update location preferences to update the API call when gpsManager is successful
    override fun onLocationRetrieved(
        latitude: Double,
        longitude: Double,
        addressDetails: String?,
        country: String?
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            locationRepository.saveCurrentLocation(addressDetails, country, latitude, longitude)
            locationViewModel.fetchLocations()
        }
    }

    override fun onLocationError(error: String) {
        Snackbar.make(binding.root, "Location Error", Snackbar.LENGTH_LONG).show()
    }
}

