package com.skysphere.skysphere.ui.home

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import android.app.AlertDialog
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HomePageFragment : Fragment(), GPSManager.GPSManagerCallback {

    // Declare the views that have been created in the XML file.
    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var weatherCodeImageView: ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var feelsLikeTemperatureTextView: TextView
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

    // Declaring the clickable upper region and the variables that will inside the alertbox.
    private lateinit var upperRegion: FrameLayout
    private var currentWindSpeed: Double = 0.0
    private var currentWindDirection: Double = 0.0
    private var currentWindGusts: Double = 0.0

    // Declaring Recyclerview and adapter which will be used to display hourly temperatures
    private lateinit var hourlyRecyclerView: RecyclerView
    private lateinit var hourlyAdapter: HourlyTemperatureAdapter

    // Declare the GPS Manager class that uses the user's location.
    private lateinit var gpsManager: GPSManager

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

        // Initializing the show more details functionality
        upperRegion = view.findViewById(R.id.upperRegion)

        // Clickable region to show wind details in an alert dialog
        upperRegion.setOnClickListener {
            showWindDetailsDialog()
        }

        // Initializing the recyclerview
        hourlyRecyclerView = view.findViewById(R.id.rvHourlyTemperatures)

        // Initializing the users preferences
        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Setting a horizontal linearlayoutmanager to arrange items horizontally
        hourlyRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // GPS client
        gpsManager = GPSManager(requireContext())

        // Call functions that get the current date and location of the user.
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

    // The wind details in an AlertDialog
    private fun showWindDetailsDialog() {
        // Getting wind data directly from the variables set in the getWeatherData() function
        val windSpeed = currentWindSpeed
        val windDirection = currentWindDirection
        val windGusts = currentWindGusts

        //  Declared a variable to store the users preferred wind speed metric unit string value set within the settings page
        val windSpeedUnit = sharedPreferences.getString("wind_speed_unit", "m/s") ?: "m/s"

        val message = """
        Wind Speed: ${"%.2f".format(windSpeed)} $windSpeedUnit
        Wind Direction: $windDirection°
        Wind Gusts: ${"%.2f".format(windGusts)} $windSpeedUnit
    """.trimIndent()
        AlertDialog.Builder(requireContext())
            .setTitle("Wind Details")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
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

    // Instead of taking local time, it now takes the date from the API call and reformats it
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(dateString: String?) {
        // Parse the input string (formatted as "yyyy-MM-dd" from the API) to a LocalDate object
        val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // Format the date
        val format = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.ENGLISH)
        val formattedDate = date.format(format)

        // Set the formatted date to the TextView
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
        val weatherService = RetrofitInstance.instance // Creates a new variable which is a RetrofitInstance.instance which builds the base URL for the API call.
        weatherService.getWeatherData(latitude, longitude, "weather_code,temperature_2m,apparent_temperature", "weather_code,temperature_2m_max,temperature_2m_min", "auto", "wind_speed_10m,wind_direction_10m,wind_gusts_10m,temperature_2m") // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    // Checks to see if we got a response from the API
                    if (response.isSuccessful) {

                        // Create variables to store the data retrieved from the API.
                        val weatherCode = response.body()?.current?.weather_code
                        val temperatureCelsius = response.body()?.current?.temperature_2m
                        val feelsLikeTemperatureCurrent = response.body()?.current?.apparent_temperature
                        val weatherType = WeatherType.fromWMO(weatherCode)

                        // Weekly Forecast Variables
                        // Max Temp
                        val day1Max = response.body()?.daily?.temperature_2m_max?.get(0)
                        val day2Max = response.body()?.daily?.temperature_2m_max?.get(1)
                        val day3Max = response.body()?.daily?.temperature_2m_max?.get(2)
                        val day4Max = response.body()?.daily?.temperature_2m_max?.get(3)
                        val day5Max = response.body()?.daily?.temperature_2m_max?.get(4)
                        val day6Max = response.body()?.daily?.temperature_2m_max?.get(5)
                        val day7Max = response.body()?.daily?.temperature_2m_max?.get(6)

                        // Min Temp
                        val day1Min = response.body()?.daily?.temperature_2m_min?.get(0)
                        val day2Min = response.body()?.daily?.temperature_2m_min?.get(1)
                        val day3Min = response.body()?.daily?.temperature_2m_min?.get(2)
                        val day4Min = response.body()?.daily?.temperature_2m_min?.get(3)
                        val day5Min = response.body()?.daily?.temperature_2m_min?.get(4)
                        val day6Min = response.body()?.daily?.temperature_2m_min?.get(5)
                        val day7Min = response.body()?.daily?.temperature_2m_min?.get(6)

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
                        val day1Date = response.body()?.daily?.time?.get(0)
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

                        // Handle hourly wind data (e.g., display the first value or calculate the average)
                        val windSpeed = response.body()?.hourly?.wind_speed_10m?.get(0) ?: 0.0
                        val windDirection = response.body()?.hourly?.wind_direction_10m?.get(0) ?: 0.0
                        val windGusts = response.body()?.hourly?.wind_gusts_10m?.get(0) ?: 0.0

                        //  Declared a variable to store the users preferred wind speed metric unit set within the settings page
                        val windSpeedUnit = sharedPreferences.getString("wind_speed_unit", "m/s")

                        // Converts the wind speed values to whichever type the user prefers
                        val displayWindSpeed = if (windSpeedUnit == "Km/h") {
                            mpsToKmph(windSpeed ?: 0.0)
                        } else if (windSpeedUnit == "Mph") {
                            mpsToMph(windSpeed ?: 0.0)
                        } else if(windSpeedUnit == "Knots") {
                            mpsToKnots(windSpeed ?: 0.0)
                        } else {
                            windSpeed ?: 0.0
                        }
                        val displayWindGusts= if (windSpeedUnit == "Km/h") {
                            mpsToKmph(windGusts ?: 0.0)
                        } else if (windSpeedUnit == "Mph") {
                            mpsToMph(windGusts ?: 0.0)
                        } else if(windSpeedUnit == "Knots") {
                            mpsToKnots(windGusts ?: 0.0)
                        } else {
                            windGusts ?: 0.0
                        }

                        // Taking only first 24 temperature values
                        val temperatures = response.body()?.hourly?.temperature_2m?.take(24) ?: emptyList()

                        // Declared a variable to store the users preferred temperature metric unit set within the settings page
                        val tempUnit = sharedPreferences.getString("temperature_unit", "Celsius")

                        // Converts the temperature to whichever type the user prefers
                        val temperature = if (tempUnit == "Celsius") {
                            temperatureCelsius ?: 0.0
                        } else {
                            celsiusToFahrenheit(temperatureCelsius ?: 0.0)
                        }
                        val feelsLikeTemperature = if (tempUnit == "Celsius") {
                            feelsLikeTemperatureCurrent ?: 0.0
                        } else {
                            celsiusToFahrenheit(feelsLikeTemperatureCurrent ?: 0.0)
                        }

                        // Converts the hourly temperature for the hourly overview to whichever type the user prefers
                        val convertedTemperatures = convertTemperatures(temperatures)

                        // Converts the max and min temperatures for the weekly overview to whichever type the user prefers
                        // Max temperature weekly overview
                        val day1MaxTemp = if (tempUnit == "Celsius"){
                            day1Max ?: 0.0
                        } else {
                            celsiusToFahrenheit(day1Max ?: 0.0)
                        }
                        val day2MaxTemp = if (tempUnit == "Celsius"){
                            day2Max ?: 0.0
                        } else {
                            celsiusToFahrenheit(day2Max ?: 0.0)
                        }
                        val day3MaxTemp = if (tempUnit == "Celsius"){
                            day3Max ?: 0.0
                        } else {
                            celsiusToFahrenheit(day3Max ?: 0.0)
                        }
                        val day4MaxTemp = if (tempUnit == "Celsius"){
                            day4Max ?: 0.0
                        } else {
                            celsiusToFahrenheit(day4Max ?: 0.0)
                        }
                        val day5MaxTemp = if (tempUnit == "Celsius"){
                            day5Max ?: 0.0
                        } else {
                            celsiusToFahrenheit(day5Max ?: 0.0)
                        }
                        val day6MaxTemp = if (tempUnit == "Celsius"){
                            day6Max ?: 0.0
                        } else {
                            celsiusToFahrenheit(day6Max ?: 0.0)
                        }
                        val day7MaxTemp = if (tempUnit == "Celsius"){
                            day7Max ?: 0.0
                        } else {
                            celsiusToFahrenheit(day7Max ?: 0.0)
                        }
                        // Min temperature weekly overview
                        val day1MinTemp = if (tempUnit == "Celsius"){
                           day1Min ?: 0.0
                        } else {
                            celsiusToFahrenheit(day1Min ?: 0.0)
                        }
                        val day2MinTemp = if (tempUnit == "Celsius"){
                            day2Min ?: 0.0
                        } else {
                            celsiusToFahrenheit(day2Min ?: 0.0)
                        }
                        val day3MinTemp = if (tempUnit == "Celsius"){
                            day3Min ?: 0.0
                        } else {
                            celsiusToFahrenheit(day3Min ?: 0.0)
                        }
                        val day4MinTemp = if (tempUnit == "Celsius"){
                            day4Min ?: 0.0
                        } else {
                            celsiusToFahrenheit(day4Min ?: 0.0)
                        }
                        val day5MinTemp = if (tempUnit == "Celsius"){
                            day5Min ?: 0.0
                        } else {
                            celsiusToFahrenheit(day5Min ?: 0.0)
                        }
                        val day6MinTemp = if (tempUnit == "Celsius"){
                            day6Min ?: 0.0
                        } else {
                            celsiusToFahrenheit(day6Min ?: 0.0)
                        }
                        val day7MinTemp = if (tempUnit == "Celsius"){
                            day7Min ?: 0.0
                        } else {
                            celsiusToFahrenheit(day7Min ?: 0.0)
                        }

                        // Sets the data retrieved from the API to the views declared at the beginning.
                        weatherCodeImageView.setImageResource(weatherType.iconRes)
                        // Displays the temperatures
                        temperatureTextView.text = "${"%.1f".format(temperature)}°"
                        feelsLikeTemperatureTextView.text = "Feels like ${"%.0f".format(feelsLikeTemperature)}°"

                        weatherStateTextView.text = "${weatherType.weatherDesc}"
                        getDate(day1Date)

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
                        day1MaxTextView.text = "${"%.0f".format(day1MaxTemp)}°"
                        day2MaxTextView.text = "${"%.0f".format(day2MaxTemp)}°"
                        day3MaxTextView.text = "${"%.0f".format(day3MaxTemp)}°"
                        day4MaxTextView.text = "${"%.0f".format(day4MaxTemp)}°"
                        day5MaxTextView.text = "${"%.0f".format(day5MaxTemp)}°"
                        day6MaxTextView.text = "${"%.0f".format(day6MaxTemp)}°"
                        day7MaxTextView.text = "${"%.0f".format(day7MaxTemp)}°"

                        day1MinTextView.text = "${"%.0f".format(day1MinTemp)}°"
                        day2MinTextView.text = "${"%.0f".format(day2MinTemp)}°"
                        day3MinTextView.text = "${"%.0f".format(day3MinTemp)}°"
                        day4MinTextView.text = "${"%.0f".format(day4MinTemp)}°"
                        day5MinTextView.text = "${"%.0f".format(day5MinTemp)}°"
                        day6MinTextView.text = "${"%.0f".format(day6MinTemp)}°"
                        day7MinTextView.text = "${"%.0f".format(day7MinTemp)}°"

                        // Display wind data
                        currentWindSpeed = displayWindSpeed
                        currentWindDirection = windDirection
                        currentWindGusts = displayWindGusts

                        hourlyAdapter = HourlyTemperatureAdapter(convertedTemperatures)
                        hourlyRecyclerView.adapter = hourlyAdapter

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

    // Converts the temperature to fahrenheit
    private fun celsiusToFahrenheit(celsius: Double): Double {
        return (celsius * (9.0/5.0)) + 32
    }

    // Converts the wind speed to Kilometers per hour
    private fun mpsToKmph(mps: Double): Double {
        return mps * 3.6
    }

    // Converts the wind speed to Miles per hour
    private fun mpsToMph(mps: Double): Double {
        return mps * 2.237
    }

    // Converts the wind speed to Knots
    private fun mpsToKnots(mps: Double): Double {
        return mps * 1.944
    }

    // Convert temperatures based on the user's preference
    private fun convertTemperatures(temperatures: List<Double>): List<Double> {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("temperature_unit", "Celsius") ?: "Celsius"
        return if (unit == "Fahrenheit") {
            temperatures.map { celsiusToFahrenheit(it) }
        } else {
            temperatures
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
