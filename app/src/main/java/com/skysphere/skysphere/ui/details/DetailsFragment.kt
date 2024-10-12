package com.skysphere.skysphere.ui.details

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.skysphere.skysphere.databinding.FragmentDetailsBinding
import com.skysphere.skysphere.services.weather.WeatherService
import com.skysphere.skysphere.services.weather.json.WeatherResults
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var weatherService: WeatherService
    private lateinit var weatherResults: WeatherResults

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        getData()

        return view
    }

    private fun getData() {
        
        weatherService.getWeather( { weatherResults ->
            // Check if current is not null
            weatherResults.current?.let { currentWeather ->
                // Now it's safe to access temperature
                binding.tvTemperature.text = currentWeather.temperature?.toString() ?: "N/A"
            } ?: run {
                // Handle the case where current is null
                binding.tvTemperature.text = "No data available"
            }
        }, { error ->
            // Handle error case
            binding.tvTemperature.text = "Error fetching weather data: ${error.message}"
        })
    }
}