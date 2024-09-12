package com.skysphere.skysphere.ui.home

import android.Manifest
import android.location.Geocoder
import android.content.Context
import android.content.SharedPreferences
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
    private lateinit var feelsLikeTemperatureTextView: TextView
    private lateinit var weatherStateTextView: TextView
    private lateinit var homeTextView: TextView

    // Declare the location client that uses the user's location.
    private lateinit var locationClient: FusedLocationProviderClient

    // Declare the shared preferences that stores the metric units
    private lateinit var sharedPreferences: SharedPreferences

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
        feelsLikeTemperatureTextView = view.findViewById(R.id.tvFeelsLikeTemperature)
        weatherStateTextView = view.findViewById(R.id.tvWeatherState)
        homeTextView = view.findViewById(R.id.text_home)

        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Call functions that get the current date and location of user.
        getDate()
        getLocation()

        return view
    }

    // Get the current date and takes the format that I make.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(){
        val date = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.ENGLISH)
        val formattedDate = date.format(format)
        dateTextView.text = formattedDate
    }

    // Gets the user location by making user accept location permissions
    private fun getLocation(){
        // This if statement checks if user has granted user location permissions (fine location and coarse location).
        if(ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED){
            // This statement occurs when permissions haven't been granted, and sends the request to the user.
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // If the location permission is granted, then it will attempt to get the last location of the user.
        locationClient.lastLocation.addOnSuccessListener { location ->
            if(location != null){ // Checks if location is received
                val geocoder = Geocoder(requireContext(),Locale.getDefault()) // Creates a Geocoder object to get address from current location
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1) // Gets the address from the latitude and longitude, and sets the max results of addresses to 1.
                if(addresses?.isNotEmpty() == true){
                    val address = addresses[0]
                    locationTextView.text = address.locality ?: "Uknown Location" // If address is found then it updates the locationTextView with the current location, or "Uknown location".
                } else {
                    locationTextView.text = "Location Not Available"
                }
                getWeatherData(location.latitude, location.longitude) // Calls the getWeatherData function and parses the users latitude and longitude to get the precise location needed for the API call.
            } else {
                locationTextView.text = "Location Not Available"
            }
        }
            // Handles errors when retrieving location.
            .addOnFailureListener { e ->
                locationTextView.text = "Location Not Available"
            }
    }

    // Used to identify permission request.
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    // Calls the API and assigns the views declared above as the data retrieved from the API. Takes in the latitude and longitude of the user.
    private fun getWeatherData(latitude: Double, longitude: Double) {
        val weatherService = RetrofitInstance.instance // Creates a new variable which is a RetrofitInstance.instance which builds the base URl for the API call.
        weatherService.getWeatherData(latitude, longitude, "weather_code,temperature_2m", "apparent_temperature") // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    // Checks to see if we got a response from the API
                    if (response.isSuccessful) {

                        // Create variables to store the data retrieved from the API.
                        val weatherCode = response.body()?.current?.weather_code
                        val temperatureCelsius = response.body()?.current?.temperature_2m
                        val feelsLikeTemperatureList = response.body()?.hourly?.apparent_temperature
                        val feelsLikeTemperatureCelsius = feelsLikeTemperatureList?.firstOrNull() ?: 0.0
                        val weatherType = WeatherType.fromWMO(weatherCode)

                        // Updates the displayed temperature to whichever type the user sets within the settings page
                        val unit = sharedPreferences.getString("temperature_unit", "Celsius")
                        val temperature = if (unit == "Celsius") {
                            temperatureCelsius ?: 0.0
                        } else {
                            celsiusToFahrenheit(temperatureCelsius ?: 0.0)
                        }
                        val feelsLikeTemperature = if (unit == "Celsius") {
                            feelsLikeTemperatureCelsius ?: 0.0
                        } else {
                            celsiusToFahrenheit(feelsLikeTemperatureCelsius ?: 0.0)
                        }

                        // Sets the data retrieved from the API to the views declared at the beginning.
                        weatherCodeImageView.setImageResource(weatherType.iconRes)
                        // Changes the metric unit to be display corresponding to the temperature
                        temperatureTextView.text = "${"%.2f".format(temperature)}${if (unit == "Celsius") "°C" else "°F"}"
                        feelsLikeTemperatureTextView.text = "Feels like ${"%.2f".format(feelsLikeTemperature)}${if (unit == "Celsius") "°C" else "°F"}"

                        weatherStateTextView.text = "${weatherType.weatherDesc}"
                    } else {
                        // If data retrieval fails, then notify user.
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

    // Converts the temperature to fahrenheit
    private fun celsiusToFahrenheit(celsius: Double): Double {
        return (celsius * (9.0/5.0)) + 32
    }

    // Handles when the user grants or denies location permissions.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If user denies then it sends the request again. If user denies again then a message is shown to inform that permissions must be allowed.
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
