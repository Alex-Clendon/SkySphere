package com.skysphere.skysphere.ui.location

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

@AndroidEntryPoint // This annotation enables Hilt's dependency injection in this Fragment
class LocationsFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)

        // Initialize the RecyclerView and Adapter
        locationsAdapter = LocationsAdapter { location ->
            // Handle the on-click
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

        binding.locationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = locationsAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
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
                        locationRepository.deleteLocation(location.area)
                        locationViewModel.fetchLocations()
                        }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.locationsRecyclerView)

        locationViewModel.locations.observe(viewLifecycleOwner) { locations ->
            locationsAdapter.updateLocations(locations)
        }

        // Observe locations from the ViewModel
        locationViewModel.locations.observe(viewLifecycleOwner) { locations ->
            locationsAdapter.updateLocations(locations)
            Log.d("LOCATIONSMODELDEBUG", "Updated locations: $locations")
        }

        // Fetch locations when the fragment is created
        locationViewModel.fetchLocations()

        // Navigation to map fragment
        val navController = findNavController()
        binding.addLocationButton.setOnClickListener {
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
}

