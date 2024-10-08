package com.skysphere.skysphere.ui.details

import android.content.Context
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
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.R
import com.skysphere.skysphere.databinding.FragmentDetailsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

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

        // Access views via the binding object
        getWeatherDetails(-36.85, 174.76)

        return view
    }

    private fun getWeatherDetails(latitude: Double, longitude: Double) {
        val weatherService =
            RetrofitInstance.instance // Creates a new variable which is a RetrofitInstance.instance which builds the base URL for the API call.
        weatherService.getDetailedWeather(
            latitude,
            longitude,
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m",
            "visibility",
            "temperature_2m_max,temperature_2m_min,sunrise,sunset,daylight_duration,uv_index_max",
            "auto",
            1
        ) // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    // Checks to see if we got a response from the API
                    if (response.isSuccessful) {
                        val weatherCode = response.body()?.current?.weather_code
                       binding.tvTemperature.text = response.body()?.current?.temperature_2m.toString() + "°"
                        val currentFeelsLike = response.body()?.current?.apparent_temperature
                    }
                }

                // If API response fails, then notify user.
                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Log.e("DetailsFragment", "API call failed: ${t.message}")
                }
            })
    }
}