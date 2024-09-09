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
import com.skysphere.skysphere.R
import com.skysphere.skysphere.API.RetrofitInstance
import com.skysphere.skysphere.API.WeatherData
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.GPSManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomePageFragment : Fragment(), GPSManager.GPSManagerCallback {

    // Declare the views that have been created in the XML file.
    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var weatherCodeImageView: ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherStateTextView: TextView
    private lateinit var homeTextView: TextView

    // Declare the GPS Manager class that uses the user's location.
    private lateinit var gpsManager: GPSManager

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

        gpsManager = GPSManager(requireContext())

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

        // If the location permission is granted, then it will attempt to get the last location of the user from GPS Manager.
        gpsManager.getCurrentLocation(this)
    }

    override fun onLocationRetrieved(latitude: Double, longitude: Double, locality: String?) {
        locationTextView.text = locality ?: "Unknown Location"
        getWeatherData(latitude, longitude)
    }

    override fun onLocationError(error: String) {
        locationTextView.text = error
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
