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
                    locationTextView.text = address.locality ?: "Unknown Location" // If address is found then it updates the locationTextView with the current location, or "Unknown location".
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
        weatherService.getWeatherData(latitude, longitude, "weather_code,temperature_2m") // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    // Checks to see if we got a response from the API
                    if (response.isSuccessful) {

                        // Create variables to store the data retrieved from the API.
                        val weatherCode = response.body()?.current?.weather_code
                        val temperature = response.body()?.current?.temperature_2m
                        val weatherType = WeatherType.fromWMO(weatherCode)

                        // Sets the data retrieved from the API to the views declared at the beginning.
                        weatherCodeImageView.setImageResource(weatherType.iconRes)
                        temperatureTextView.text = "${temperature}°C"
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
