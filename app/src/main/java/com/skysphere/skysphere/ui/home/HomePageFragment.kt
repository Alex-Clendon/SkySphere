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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HomePageFragment : Fragment() {

    // Declare the views that have been created in the XML file.
    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var weatherCodeImageView: ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherStateTextView: TextView
    private lateinit var homeTextView: TextView
    private lateinit var setCurrentLocationButton: ImageButton

    // Weekly Forecast Variables
    private lateinit var day2TextView: TextView
    private lateinit var day3TextView: TextView
    private lateinit var day4TextView: TextView
    private lateinit var day5TextView: TextView
    private lateinit var day6TextView: TextView
    private lateinit var day7TextView: TextView

    private lateinit var day1IconImageView: ImageView
    private lateinit var day2IconImageView: ImageView
    private lateinit var day3IconImageView: ImageView
    private lateinit var day4IconImageView: ImageView
    private lateinit var day5IconImageView: ImageView
    private lateinit var day6IconImageView: ImageView
    private lateinit var day7IconImageView: ImageView

    private lateinit var day1MaxTextView: TextView
    private lateinit var day2MaxTextView: TextView
    private lateinit var day3MaxTextView: TextView
    private lateinit var day4MaxTextView: TextView
    private lateinit var day5MaxTextView: TextView
    private lateinit var day6MaxTextView: TextView
    private lateinit var day7MaxTextView: TextView

    private lateinit var day1MinTextView: TextView
    private lateinit var day2MinTextView: TextView
    private lateinit var day3MinTextView: TextView
    private lateinit var day4MinTextView: TextView
    private lateinit var day5MinTextView: TextView
    private lateinit var day6MinTextView: TextView
    private lateinit var day7MinTextView: TextView

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

        // Weekly Forecast variables
        day2TextView = view.findViewById(R.id.day2_day)
        day3TextView = view.findViewById(R.id.day3_day)
        day4TextView = view.findViewById(R.id.day4_day)
        day5TextView = view.findViewById(R.id.day5_day)
        day6TextView = view.findViewById(R.id.day6_day)
        day7TextView = view.findViewById(R.id.day7_day)

        day1IconImageView = view.findViewById(R.id.day1_icon)
        day2IconImageView = view.findViewById(R.id.day2_icon)
        day3IconImageView = view.findViewById(R.id.day3_icon)
        day4IconImageView = view.findViewById(R.id.day4_icon)
        day5IconImageView = view.findViewById(R.id.day5_icon)
        day6IconImageView = view.findViewById(R.id.day6_icon)
        day7IconImageView = view.findViewById(R.id.day7_icon)

        day1MaxTextView = view.findViewById(R.id.day1_max)
        day2MaxTextView = view.findViewById(R.id.day2_max)
        day3MaxTextView = view.findViewById(R.id.day3_max)
        day4MaxTextView = view.findViewById(R.id.day4_max)
        day5MaxTextView = view.findViewById(R.id.day5_max)
        day6MaxTextView = view.findViewById(R.id.day6_max)
        day7MaxTextView = view.findViewById(R.id.day7_max)

        day1MinTextView = view.findViewById(R.id.day1_min)
        day2MinTextView = view.findViewById(R.id.day2_min)
        day3MinTextView = view.findViewById(R.id.day3_min)
        day4MinTextView = view.findViewById(R.id.day4_min)
        day5MinTextView = view.findViewById(R.id.day5_min)
        day6MinTextView = view.findViewById(R.id.day6_min)
        day7MinTextView = view.findViewById(R.id.day7_min)
        // End of Weekly Forecast variables

        // Location Client
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
        weatherService.getWeatherData(latitude, longitude, "weather_code,temperature_2m", "weather_code,temperature_2m_max,temperature_2m_min") // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
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
                        val day1Max = response.body()?.daily?.temperature_2m_max?.get(0)?.roundToInt()
                        val day2Max = response.body()?.daily?.temperature_2m_max?.get(1)?.roundToInt()
                        val day3Max = response.body()?.daily?.temperature_2m_max?.get(2)?.roundToInt()
                        val day4Max = response.body()?.daily?.temperature_2m_max?.get(3)?.roundToInt()
                        val day5Max = response.body()?.daily?.temperature_2m_max?.get(4)?.roundToInt()
                        val day6Max = response.body()?.daily?.temperature_2m_max?.get(5)?.roundToInt()
                        val day7Max = response.body()?.daily?.temperature_2m_max?.get(6)?.roundToInt()

                        // Min Temp
                        val day1Min = response.body()?.daily?.temperature_2m_min?.get(0)?.roundToInt()
                        val day2Min = response.body()?.daily?.temperature_2m_min?.get(1)?.roundToInt()
                        val day3Min = response.body()?.daily?.temperature_2m_min?.get(2)?.roundToInt()
                        val day4Min = response.body()?.daily?.temperature_2m_min?.get(3)?.roundToInt()
                        val day5Min = response.body()?.daily?.temperature_2m_min?.get(4)?.roundToInt()
                        val day6Min = response.body()?.daily?.temperature_2m_min?.get(5)?.roundToInt()
                        val day7Min = response.body()?.daily?.temperature_2m_min?.get(6)?.roundToInt()

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

                        //Dates
                        val day2Date = response.body()?.daily?.time?.get(1)
                        val day3Date = response.body()?.daily?.time?.get(2)
                        val day4Date = response.body()?.daily?.time?.get(3)
                        val day5Date = response.body()?.daily?.time?.get(4)
                        val day6Date = response.body()?.daily?.time?.get(5)
                        val day7Date = response.body()?.daily?.time?.get(6)

                        // Parse the dates into the getDayName() function
                        val day2Name = getDayName(day2Date)
                        val day3Name = getDayName(day3Date)
                        val day4Name = getDayName(day4Date)
                        val day5Name = getDayName(day5Date)
                        val day6Name = getDayName(day6Date)
                        val day7Name = getDayName(day7Date)


                        weatherCodeImageView.setImageResource(weatherType.iconRes)
                        temperatureTextView.text = "${temperature}°C"
                        weatherStateTextView.text = "${weatherType.weatherDesc}"

                        // Set Weekly Forecast data
                        // Days
                        day2TextView.text = "${day2Name}"
                        day3TextView.text = "${day3Name}"
                        day4TextView.text = "${day4Name}"
                        day5TextView.text = "${day5Name}"
                        day6TextView.text = "${day6Name}"
                        day7TextView.text = "${day7Name}"

                        // Icons
                        day1IconImageView.setImageResource(day1WeatherType.iconRes)
                        day2IconImageView.setImageResource(day2WeatherType.iconRes)
                        day3IconImageView.setImageResource(day3WeatherType.iconRes)
                        day4IconImageView.setImageResource(day4WeatherType.iconRes)
                        day5IconImageView.setImageResource(day5WeatherType.iconRes)
                        day6IconImageView.setImageResource(day6WeatherType.iconRes)
                        day7IconImageView.setImageResource(day7WeatherType.iconRes)

                        // Temperature Data
                        day1MaxTextView.text = "${day1Max}°"
                        day2MaxTextView.text = "${day2Max}°"
                        day3MaxTextView.text = "${day3Max}°"
                        day4MaxTextView.text = "${day4Max}°"
                        day5MaxTextView.text = "${day5Max}°"
                        day6MaxTextView.text = "${day6Max}°"
                        day6MaxTextView.text = "${day7Max}°"

                        day1MinTextView.text = "${day1Min}°"
                        day2MinTextView.text = "${day2Min}°"
                        day3MinTextView.text = "${day3Min}°"
                        day4MinTextView.text = "${day4Min}°"
                        day5MinTextView.text = "${day5Min}°"
                        day6MinTextView.text = "${day6Min}°"
                        day7MinTextView.text = "${day7Min}°"

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

    // Function to convert date string into day name
    fun getDayName(dateString: String?): String {
        return try {
            // Parse the date string (The format from the API doc is "yyyy-MM-dd")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)

            // Format the date to get the day name
            val outputFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            "Unknown"
        }
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