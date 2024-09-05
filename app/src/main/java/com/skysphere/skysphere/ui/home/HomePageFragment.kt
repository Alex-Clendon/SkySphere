package com.skysphere.skysphere.ui.home

import android.Manifest
import android.location.Geocoder
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.skysphere.skysphere.R
import com.skysphere.skysphere.RetrofitInstance
import com.skysphere.skysphere.WeatherData
import com.skysphere.skysphere.WeatherType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomePageFragment : Fragment() {

    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var weatherCodeImageView: ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherStateTextView: TextView
    private lateinit var homeTextView: TextView

    private lateinit var locationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dateTextView = view.findViewById(R.id.tvDate)
        locationTextView = view.findViewById(R.id.tvLocation)
        weatherCodeImageView = view.findViewById(R.id.ivWeatherIcon)
        temperatureTextView = view.findViewById(R.id.tvTemperature)
        weatherStateTextView = view.findViewById(R.id.tvWeatherState)
        homeTextView = view.findViewById(R.id.text_home)

        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        getDate()
        getLocation()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(){
        val date = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.ENGLISH)
        val formattedDate = date.format(format)
        dateTextView.text = formattedDate
    }

    private fun getLocation(){
        if(ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        locationClient.lastLocation.addOnSuccessListener { location ->
            if(location != null){
                val geocoder = Geocoder(requireContext(),Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if(addresses?.isNotEmpty() == true){
                    val address = addresses[0]
                    locationTextView.text = address.locality ?: "Uknown Location"
                } else {
                    locationTextView.text = "Location Not Available"
                }
                getWeatherData(location.latitude, location.longitude)
            } else {
                locationTextView.text = "Location Not Available"
            }
        }
            .addOnFailureListener { e ->
                locationTextView.text = "Location Not Available"
            }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        val weatherService = RetrofitInstance.instance
        weatherService.getWeatherData(latitude, longitude, "weather_code,temperature_2m")
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if (response.isSuccessful) {

                        val weatherCode = response.body()?.current?.weather_code
                        val temperature = response.body()?.current?.temperature_2m

                        val weatherType = WeatherType.fromWMO(weatherCode)

                        weatherCodeImageView.setImageResource(weatherType.iconRes)
                        temperatureTextView.text = "${temperature}°C"
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation()
                } else {
                    locationTextView.text = "You must allow location permission to get weather data"
                }
                return
            }
        }
    }

}