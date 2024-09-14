package com.skysphere.skysphere.ui.home

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.skysphere.skysphere.R
import com.skysphere.skysphere.API.RetrofitInstance
import com.skysphere.skysphere.API.WeatherData
import com.skysphere.skysphere.API.WeatherType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomePageFragment : Fragment() {

    // Declare the views that have been created in the XML file.
    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var weatherCodeImageView: ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherStateTextView: TextView
    private lateinit var homeTextView: TextView
    private lateinit var setCurrentLocationButton: ImageButton

    // Declare the location client that uses the user's location.
    private lateinit var locationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Assign the views to variables declared above.
        dateTextView = view.findViewById(R.id.tvDate)
        locationTextView = view.findViewById(R.id.tvLocation)
        weatherCodeImageView = view.findViewById(R.id.ivWeatherIcon)
        temperatureTextView = view.findViewById(R.id.tvTemperature)
        weatherStateTextView = view.findViewById(R.id.tvWeatherState)
        homeTextView = view.findViewById(R.id.text_home)

        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Call functions that get the current date and location of the user.
        getDate()
        if (isCustomLocationSet())  // Conditional statement to check if the user has a custom location selected
        {
            getCustomLocationWeather()
        }
        else {
            getLocation() // Get weather based on phone's current location
        }

        setCurrentLocationButton = view.findViewById(R.id.currentLocationButton) // Initialise current location button

        setCurrentLocationButton.setOnClickListener { // Clear custom location preferences and get data from user's current location when clicked.
            clearCustomLocationPreferences()
            getLocation()
            Toast.makeText(requireContext(), "Location Updated", Toast.LENGTH_LONG).show()
        }

        return view
    }

    private fun clearCustomLocationPreferences() { // Clears custom location preferences
        val sharedPrefs = requireContext().getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            clear()
            apply()
        }
    }

    private fun isCustomLocationSet(): Boolean { // Check if user has a custom location selected
        val sharedPrefs = requireContext().getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.contains("latitude") && sharedPrefs.contains("longitude")
    }

    // Get weather for the custom location
    private fun getCustomLocationWeather() {
        val sharedPrefs = requireContext().getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
        val latitude = sharedPrefs.getFloat("latitude", 0f).toDouble()
        val longitude = sharedPrefs.getFloat("longitude", 0f).toDouble()
        val placeName = sharedPrefs.getString("place_name", "Custom Location")

        locationTextView.text = placeName // Update location text with the custom place name
        getWeatherData(latitude, longitude) // Get weather data for the custom location
    }

    // Gets the current date and takes the format that I make.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(){
        val date = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.ENGLISH)
        val formattedDate = date.format(format)
        dateTextView.text = formattedDate
    }

    // Gets the user location by making user accept location permissions
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
                    locationTextView.text = address.locality ?: "Unknown Location"
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

    //Used to identify permission request.
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    // Calls the API and assigns the views declared above as the data retrieved from the API. Takes in the latitude and longitude of the user.
    private fun getWeatherData(latitude: Double, longitude: Double) {
        val weatherService = RetrofitInstance.instance // Creates a new variable which is a RetrofitInstance.instance which builds the base URL for the API call.
        weatherService.getWeatherData(latitude, longitude, "weather_code,temperature_2m", "weather_code, temperature_2m_max, temperature_2m_min") // Cals the getWeatherData function and parses the user location variables, and other variables needed from the API.
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    // Checks to see if we got a response from the API
                    if (response.isSuccessful) {

                        // Create variables to store the data retrieved from the API.
                        val weatherCode = response.body()?.current?.weather_code
                        val temperature = response.body()?.current?.temperature_2m
                        val weatherType = WeatherType.fromWMO(weatherCode)

                        // Weekly Forecast Variables
                        // Max Temp
                        val day1Max = response.body()?.daily?.temperature_2m_max?.get(0) ?: 0.0
                        val day2Max = response.body()?.daily?.temperature_2m_max?.get(1) ?: 0.0
                        val day3Max = response.body()?.daily?.temperature_2m_max?.get(2) ?: 0.0
                        val day4Max = response.body()?.daily?.temperature_2m_max?.get(3) ?: 0.0
                        val day5Max = response.body()?.daily?.temperature_2m_max?.get(4) ?: 0.0
                        val day6Max = response.body()?.daily?.temperature_2m_max?.get(5) ?: 0.0
                        val day7Max = response.body()?.daily?.temperature_2m_max?.get(6) ?: 0.0

                        // Min Temp
                        val day1Min = response.body()?.daily?.temperature_2m_min?.get(0) ?: 0.0
                        val day2Min = response.body()?.daily?.temperature_2m_min?.get(1) ?: 0.0
                        val day3Min = response.body()?.daily?.temperature_2m_min?.get(2) ?: 0.0
                        val day4Min = response.body()?.daily?.temperature_2m_min?.get(3) ?: 0.0
                        val day5Min = response.body()?.daily?.temperature_2m_min?.get(4) ?: 0.0
                        val day6Min = response.body()?.daily?.temperature_2m_min?.get(5) ?: 0.0
                        val day7Min = response.body()?.daily?.temperature_2m_min?.get(6) ?: 0.0

                        // Weather Code
                        val day1WeatherCode = response.body()?.daily?.weather_code?.get(0) ?: 0
                        val day2WeatherCode = response.body()?.daily?.weather_code?.get(1) ?: 0
                        val day3WeatherCode = response.body()?.daily?.weather_code?.get(2) ?: 0
                        val day4WeatherCode = response.body()?.daily?.weather_code?.get(3) ?: 0
                        val day5WeatherCode = response.body()?.daily?.weather_code?.get(4) ?: 0
                        val day6WeatherCode = response.body()?.daily?.weather_code?.get(5) ?: 0
                        val day7WeatherCode = response.body()?.daily?.weather_code?.get(6) ?: 0

                        // Weather Type
                        val day1WeatherType = WeatherType.fromWMO(day1WeatherCode)
                        val day2WeatherType = WeatherType.fromWMO(day2WeatherCode)
                        val day3WeatherType = WeatherType.fromWMO(day3WeatherCode)
                        val day4WeatherType = WeatherType.fromWMO(day4WeatherCode)
                        val day5WeatherType = WeatherType.fromWMO(day5WeatherCode)
                        val day6WeatherType = WeatherType.fromWMO(day6WeatherCode)
                        val day7WeatherType = WeatherType.fromWMO(day7WeatherCode)



                        weatherCodeImageView.setImageResource(weatherType.iconRes)
                        temperatureTextView.text = "${temperature}°C"
                        weatherStateTextView.text = "${weatherType.weatherDesc}"
                    } else {
                        homeTextView.text = "Failed to get data"
                        temperatureTextView.text = "Failed to get data"
                    }
                }

                // If API response fails, then notify user.
                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    homeTextView.text = "Error: ${t.message}"
                    temperatureTextView.text = "Error: ${t.message}"
                }
            })
    }

    // Handles when the user grants or denies location permissions.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If user denies then it sends the request again. If user denies again then a message is shown to inform that permission must be allowed.
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