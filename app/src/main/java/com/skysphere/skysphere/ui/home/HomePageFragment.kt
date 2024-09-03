package com.skysphere.skysphere.ui.home

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.skysphere.skysphere.R
import com.skysphere.skysphere.RetrofitInstance
import com.skysphere.skysphere.WeatherData
import com.skysphere.skysphere.WeatherType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class HomePageFragment : Fragment() {

    private lateinit var weatherCodeImageView: ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherStateTextView: TextView
    private lateinit var homeTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        weatherCodeImageView = view.findViewById(R.id.ivWeatherIcon)
        temperatureTextView = view.findViewById(R.id.tvTemperature)
        weatherStateTextView = view.findViewById(R.id.tvWeatherState)
        homeTextView = view.findViewById(R.id.text_home)

        fetchWeatherData()

        return view
    }

    private fun fetchWeatherData() {
        val weatherService = RetrofitInstance.instance
        weatherService.getWeatherData(40.90, 174.89, "weather_code,temperature_2m")
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if (response.isSuccessful) {
                        val currentHour = LocalDateTime.now().hour

                        val weatherCode = response.body()?.hourly?.weather_code
                        val temperature = response.body()?.hourly?.temperature_2m

                        val currentWeatherCode = weatherCode?.get(currentHour) ?: 0
                        val currentTemperature = temperature?.get(currentHour) ?: 0.0

                        val weatherType = WeatherType.fromWMO(currentWeatherCode)

                        weatherCodeImageView.setImageResource(weatherType.iconRes)
                        temperatureTextView.text = "${currentTemperature}°C"
                        weatherStateTextView.text = "${weatherType.weatherDesc}"
                    } else {
                        homeTextView.text = "Failed to get data"
                        temperatureTextView.text = "Failed to get data"
                    }
                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    homeTextView.text = "Error: ${t.message}"
                    temperatureTextView.text = "Error: ${t.message}"
                }
            })
    }

}