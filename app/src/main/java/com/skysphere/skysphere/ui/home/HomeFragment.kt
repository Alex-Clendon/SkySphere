package com.skysphere.skysphere.ui.home

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.skysphere.skysphere.GPSManager
import com.skysphere.skysphere.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var gpsManager: GPSManager
    private var isGpsEnabled = false // To track the toggle GPS toggle state

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Initialize GPS Manager
        gpsManager = GPSManager(requireContext())

        // Setup toggle switch for location sourcing
        val locationToggle: Switch = binding.locationToggle

        // Set up the toggle to switch between GPS and manual location
        locationToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // GPS-based weather data
                enableGpsLocation()
            } else {
                // Manually entered location-based weather data
                disableGpsLocation()
            }
        }
        return root
    }

    private fun enableGpsLocation() {
        isGpsEnabled = true
        // Start GPS updates
        gpsManager.startLocationUpdates()

        // Fetch weather based on the current GPS location
        val currentLocation = gpsManager.getCurrentLocation()
        if (currentLocation != null) {
            fetchWeather(currentLocation.latitude, currentLocation.longitude)
        } else {
            // Handle the case where location is null
        }
    }

    private fun disableGpsLocation() {
        isGpsEnabled = false
        // Stop GPS updates
        gpsManager.stopLocationUpdates()

        // Fetch weather based on manually entered location (Manual location retrieval still being implemented)

        val manualLocation = getManualLocation()
        fetchWeather(manualLocation.latitude, manualLocation.longitude)
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        // Implement the logic to fetch weather data using the provided latitude and longitude
    }

    private fun getManualLocation(): Location {
        // Placeholder for manual location logic
        return Location("manual").apply {
            latitude = 37.7749 // Example latitude
            longitude = -122.4194 // Example longitude
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}