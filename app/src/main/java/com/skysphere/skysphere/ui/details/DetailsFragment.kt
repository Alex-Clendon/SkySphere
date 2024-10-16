package com.skysphere.skysphere.ui.details

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.skysphere.skysphere.R
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.weather.WeatherResults
import com.skysphere.skysphere.databinding.FragmentDetailsBinding
import com.skysphere.skysphere.services.weather.WeatherService
import com.skysphere.skysphere.services.weather.json.ApiResults
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModel: WeatherViewModel // Hilt will provide this

    private var weatherResults: WeatherResults? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.weatherResults.observe(this) { results ->
            weatherResults = results
            Log.d("Database Operation:", "Fragment Updated")
            getData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.light_grey)
        return binding.root
    }

    private fun getData() {
        weatherResults?.let {
            binding.tvTemperature.text = it.current?.temperature.toString()
            binding.tvTemperatureUnit.text = it.current?.tempUnit
            binding.tvWeatherState.text = it.current?.weatherText
        } ?: run {
            binding.tvTemperature.text = "No data available"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leaks
        val navController = findNavController()
        // Use NavController to navigate to HomeFragment
        navController.navigate(R.id.nav_home)
    }
}


   /* private fun getWeatherDetails(latitude: Double, longitude: Double) {
        val weatherService =
            RetrofitInstance.instance // Creates a new variable which is a RetrofitInstance.instance which builds the base URL for the API call.
        weatherService.getWeatherData2(
            -36.85, // After testing, use location.latitude,
            174.76, // After testing, use location.longitude,
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m",
            "visibility",
            "temperature_2m_max,temperature_2m_min,sunrise,sunset,daylight_duration,uv_index_max",
            "auto",
            1
        ) // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
            .enqueue(object : Callback<WeatherResults> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherResults>, response: Response<WeatherResults>) {
                    // Checks to see if we got a response from the API
                    if (response.isSuccessful) {
                        //val weatherCode = response.body()?.current?.weather_code
                        Log.d("DetailsFragment", "Response body: $weatherResults")

                        weatherResults?.let {
                            Log.d(
                                "DetailsFragment",
                                "Current Weather Code: ${it.current?.weatherCode}"
                            )
                            Log.d(
                                "DetailsFragment",
                                "Current Temperature: ${it.current?.temperature}"
                            )
                            binding.tvTemperature.text =
                                response.body()?.current?.temperature.toString()
                            //val currentFeelsLike = response.body()?.current?.apparent_temperature
                        }
                    }
                    else {
                        binding.tvTemperature.text = "Failed"
                    }
                }

                // If API response fails, then notify user.
                override fun onFailure(call: Call<WeatherResults>, t: Throwable) {
                    Log.e("DetailsFragment", "API call really failed: ${t.message}")
                }
            })
    }*/
