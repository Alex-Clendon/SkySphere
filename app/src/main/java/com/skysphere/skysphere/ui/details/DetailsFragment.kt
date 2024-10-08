package com.skysphere.skysphere.ui.details

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.skysphere.skysphere.API.RetrofitInstance
import com.skysphere.skysphere.API.WeatherData
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_details, container, false)

        getWeatherDetails(-36.85, 174.76)

        return view
    }

    private fun getWeatherDetails(latitude: Double, longitude: Double) {
        val weatherService =
            RetrofitInstance.instance // Creates a new variable which is a RetrofitInstance.instance which builds the base URL for the API call.
        weatherService.getDetailedWeather(
            latitude,
            longitude,
            "weather_code,temperature_2m,apparent_temperature",
            "weather_code,temperature_2m_max,temperature_2m_min",
            "auto",
            "wind_speed_10m,wind_direction_10m,wind_gusts_10m,temperature_2m"
        ) // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    // Checks to see if we got a response from the API
                    if (response.isSuccessful) {

                    }
                }

                // If API response fails, then notify user.
                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                }
            })
    }
}