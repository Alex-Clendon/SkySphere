package com.skysphere.skysphere.ui.details

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.skysphere.skysphere.API.RetrofitInstance
import com.skysphere.skysphere.API.WeatherData
import com.skysphere.skysphere.databinding.FragmentDetailsBinding
import com.skysphere.skysphere.services.weather.WeatherService
import com.skysphere.skysphere.services.weather.json.WeatherResults
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
}