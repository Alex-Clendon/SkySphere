package com.skysphere.skysphere.ui.location

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.skysphere.skysphere.R
import com.skysphere.skysphere.databinding.FragmentLocationsBinding
import com.skysphere.skysphere.ui.adapters.LocationsAdapter
import com.skysphere.skysphere.ui.adapters.NewsAdapter
import com.skysphere.skysphere.view_models.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.skysphere.skysphere.background.WeatherUpdateWorker
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.repositories.LocationRepository
import com.skysphere.skysphere.ui.adapters.DailyWeatherAdapter
import com.skysphere.skysphere.widgets.SkySphereWidget
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.concurrent.TimeUnit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.skysphere.skysphere.GPSManager
import com.skysphere.skysphere.ui.home.HomePageFragment

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class LocationsFragment : Fragment(), GPSManager.GPSManagerCallback {

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationsAdapter: LocationsAdapter

    // Use Hilt to inject the ViewModel
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
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.background_white)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background_white
                )
            )
        ) // Action Bar Color
        // Change default app colours
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.background_white) // Status Bar Color
        actionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle("")
        // Initialize the RecyclerView and Adapter
        locationsAdapter = LocationsAdapter { location ->
            // Check if there is an internet connection
            val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            val isConnected = activeNetwork?.isConnectedOrConnecting == true

            if (!isConnected) {
                // Show a Snackbar if there is no internet connection
                Snackbar.make(binding.root, "Network Unavailable", Snackbar.LENGTH_LONG).show()
            } else {
                // Handle the on-click action if internet is available
                settingsManager.saveLocation(location.latitude, location.longitude, location.area)

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


        binding.locationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = locationsAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val location = locationsAdapter.getLocationAt(position)
                lifecycleScope.launch {
                    val locations = locationRepository.getAllLocations() // Fetch current list of locations

                    if (settingsManager.getCustomLocation() == location.area) {
                        // If there are exactly 2 locations, delete the swiped one and update settings with the remaining location
                        locationRepository.deleteLocation(location.area)
                        val remainingLocation = locations.firstOrNull { it.id != location.id } // Get the remaining location
                        remainingLocation?.let {
                            settingsManager.saveLocation(it.latitude, it.longitude, it.area)
                            Log.d("DEBUGDEBUG", "${it.area}")
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
                        locationViewModel.fetchLocations() // Refresh the list of locations
                    } else {
                        // Otherwise, delete the location normally
                        locationRepository.deleteLocation(location.area)
                        locationViewModel.fetchLocations()
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.locationsRecyclerView)

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

        // Navigation to map fragment
        val navController = findNavController()
        binding.addLocationCard.setOnClickListener {
            navController.navigate(R.id.nav_map)
        }

        return binding.root
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

