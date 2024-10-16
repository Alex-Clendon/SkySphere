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
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.weather.WeatherResults
import com.skysphere.skysphere.ui.adapters.DailyWeatherAdapter
import com.skysphere.skysphere.widgets.SkySphereWidget
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class HomePageFragment : Fragment(), GPSManager.GPSManagerCallback {


    @Inject
    lateinit var viewModel: WeatherViewModel // Hilt will provide this

    private var weatherResults: WeatherResults? = null
    private lateinit var dailyWeatherAdapter: DailyWeatherAdapter
    private lateinit var dailyRecyclerView: RecyclerView
    // Declare the views that have been created in the XML file.

    //Current Weather
    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var weatherCodeImageView: ImageView
    private lateinit var temperatureTextView: TextView
    private lateinit var temperatureUnit: TextView
    private lateinit var feelsLikeTemperatureTextView: TextView
    private lateinit var weatherStateTextView: TextView
    private lateinit var homeTextView: TextView
    private lateinit var setCurrentLocationButton: ImageButton
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var textToSpeechBtn: ImageButton

    // Declaring the clickable upper region and the variables that will inside the alertbox.
    private lateinit var upperRegion: FrameLayout
    private var currentWindSpeed: Double = 0.0
    private var currentWindDirection: Double = 0.0
    private var currentWindGusts: Double = 0.0

    // Declaring chart which will be used to display hourly temperatures
    private lateinit var temperatureChart: LineChart


    // Declare the GPS Manager class that uses the user's location.
    private lateinit var gpsManager: GPSManager

    // Declare the shared preferences that stores the metric units
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.weatherResults.observe(this) { results ->
            weatherResults = results
            Log.d("Daily Operation", "Daily Results: ${weatherResults?.daily}")
            getData()
        }
    }

    private fun getData() {

        // Sets the data retrieved from the API to the views declared at the beginning.
        weatherResults?.let {
            // Current
            temperatureTextView.text = it.current?.temperature.toString()
            temperatureUnit.text = it.current?.tempUnit
            weatherStateTextView.text = it.current?.weatherText
            it.current?.weatherType?.let { it1 -> weatherCodeImageView.setImageResource(it1.iconRes) }
            feelsLikeTemperatureTextView.text = "Feels like " + it.current?.apparentTemperature.toString() + "°"
            dateTextView.text = it.current?.date

            // Weekly
            dailyWeatherAdapter = DailyWeatherAdapter(weatherResults?.daily)
            dailyRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = dailyWeatherAdapter
            }
        } ?: run {

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)

        dailyRecyclerView = view.findViewById((R.id.dailyRecycler))

        // Assign the views to variables declared above.
        dateTextView = view.findViewById(R.id.tvDate)
        locationTextView = view.findViewById(R.id.tvLocation)
        weatherCodeImageView = view.findViewById(R.id.ivWeatherIcon)
        temperatureTextView = view.findViewById(R.id.tvTemperature)
        temperatureUnit = view.findViewById(R.id.tvTemperatureUnit)
        feelsLikeTemperatureTextView = view.findViewById(R.id.tvFeelsLikeTemperature)
        weatherStateTextView = view.findViewById(R.id.tvWeatherState)
        homeTextView = view.findViewById(R.id.text_home)
        // End of Weekly Forecast variables

        // Initializing the show more details functionality
        upperRegion = view.findViewById(R.id.upperRegion)

        // Clickable region to show wind details in an alert dialog
        upperRegion.setOnClickListener {
            val navController = findNavController()
            // Use NavController to navigate to HomeFragment
            navController.navigate(R.id.nav_details)
        }


        // Initializing the users preferences
        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Initializing the chart
        temperatureChart = view.findViewById(R.id.temperatureChart)

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
            updateWidget()
        }

        // Text to speech button
        textToSpeechBtn = view.findViewById(R.id.ttsBtn)
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if(status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(requireContext(), "Language not supported", Toast.LENGTH_LONG).show()
                }
            }
        }
        // When clicked AI will speak to user
        textToSpeechBtn.setOnClickListener {
            textToSpeechDialog()
        }

        /*
        val detailsRedirect = view.findViewById<TextView>(R.id.day2_day)
        // Set on click listener
        detailsRedirect.setOnClickListener {
            val navController = findNavController()
            // Use NavController to navigate to HomeFragment
            navController.navigate(R.id.nav_details)
        }*/

        return view
    }

    // Text for text to speech
    private fun textToSpeechDialog(){
        // Getting data for text to speech
        val location = locationTextView.text.toString().trim()
        val date = dateTextView.text.toString().trim()
        val temperatureCelsius = temperatureTextView.text.toString().trim()
        val weatherType = weatherStateTextView.text.toString().trim()
        val feelsLikeTemperatureCurrent = feelsLikeTemperatureTextView.text.toString().trim()

        val tempUnit = sharedPreferences.getString("temperature_unit", "Celsius") ?: "Celsius"

        if (location.isNotEmpty() && date.isNotEmpty() && temperatureCelsius.isNotEmpty() && weatherType.isNotEmpty() && feelsLikeTemperatureCurrent.isNotEmpty()) {
            // Combining the text made above
            val textToSpeak = "Your current location is $location. " +
                    "The date is $date. " +
                    "The current temperature in $location is $temperatureCelsius $tempUnit." +
                    "But it currently $feelsLikeTemperatureCurrent $tempUnit. " +
                    "The current weather is $weatherType. "

            // Text to speech the combined text
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            val textToSpeak = "No data to speak. Data currently unavailable."
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }

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

    private fun setupTemperatureChart(temperatures: List<Double>, times: List<String>) {
        // Creating entry points for line chart
        val entries = temperatures.mapIndexed { index, temp ->
            Entry(index.toFloat(), temp.toFloat())
        }

        // Dataset custom appearance
        val dataSet = LineDataSet(entries, "Hourly Temperature")
        dataSet.valueTextColor = Color.WHITE
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.WHITE)
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 10f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.BLACK
        dataSet.fillAlpha = 50

        val lineData = LineData(dataSet)

        // General appearance/behaviour configuration
        temperatureChart.data = lineData
        temperatureChart.description.isEnabled = false
        temperatureChart.legend.isEnabled = false
        temperatureChart.setTouchEnabled(true)
        temperatureChart.isDoubleTapToZoomEnabled = false
        temperatureChart.isDragEnabled = true

        // X-axis configuration
        val xAxis = temperatureChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(times)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelCount = times.size
        xAxis.textColor = Color.WHITE

        // Y-axis configuration
        val leftAxis = temperatureChart.axisLeft
        leftAxis.axisMinimum = temperatures.minOrNull()?.toFloat()?.minus(2f) ?: 0f
        leftAxis.axisMaximum = temperatures.maxOrNull()?.toFloat()?.plus(2f) ?: 30f
        temperatureChart.axisRight.isEnabled = false
        leftAxis.textColor = Color.WHITE

        // Making chart scrollable
        temperatureChart.setVisibleXRangeMaximum(6f)
        temperatureChart.moveViewToX(0f)

        // Refreshing the chart
        temperatureChart.invalidate()


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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCustomLocationWeather() {
        val sharedPrefs = requireContext().getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
        val latitude = sharedPrefs.getFloat("latitude", 0f).toDouble()
        val longitude = sharedPrefs.getFloat("longitude", 0f).toDouble()
        val placeName = sharedPrefs.getString("place_name", "Custom Location")

        locationTextView.text = placeName // Update location text with the custom place name
        getWeatherData(latitude, longitude) // Get weather data for the custom location
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLocationRetrieved(latitude: Double, longitude: Double, locality: String?) {
        locationTextView.text = locality ?: "Unknown Location"
        saveLocationToPrefs(latitude, longitude) // Save location to SharedPreferences
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getWeatherData(latitude: Double, longitude: Double) {

        val weatherService = RetrofitInstance.getInstance(false)// Creates a new variable which is a RetrofitInstance.instance which builds the base URL for the API call.
        weatherService.getWeatherData(latitude, longitude, "weather_code,temperature_2m,apparent_temperature", "weather_code,temperature_2m_max,temperature_2m_min", "auto", "wind_speed_10m,wind_direction_10m,wind_gusts_10m,temperature_2m") // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
            .enqueue(object : Callback<WeatherData> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    // Checks to see if we got a response from the API
                    if (response.isSuccessful) {

                        // Get the current time
                        val currentTime = response.body()?.current?.time

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

                        // Save current weather data for use in recommendations fragment
                        val sharedPrefs = requireContext().getSharedPreferences("weather_data", Context.MODE_PRIVATE)
                        with(sharedPrefs.edit()) {
                            if (temperatureCelsius != null) {
                                putFloat("temperature_celsius", temperatureCelsius.toFloat())
                            }
                            putInt("weather_code", weatherCode ?: 0)
                            putLong("last_updated", System.currentTimeMillis())
                            putString("current_time", currentTime)
                            apply()
                        }

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

                        // Setting the times associated with the temperature
                        val times = List(24) { index -> String.format("%02d:00", index) }

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

                        // Setting up Temperature chart after conversion to ensure graph is also updated with conversions
                        setupTemperatureChart(convertedTemperatures, times)

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

                        // Display wind data
                        currentWindSpeed = displayWindSpeed
                        currentWindDirection = windDirection
                        currentWindGusts = displayWindGusts


                    } else {
                        // If data retrieval fails, then notify user.
                        homeTextView.text = "Failed to get data"
                        temperatureTextView.text = "Failed to get data"
                    }
                }

                // If API response fails, then notify user.
                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    homeTextView.text = "Error: ${t.message}"
                    //temperatureTextView.text = "Error: ${t.message}"
                }
            })
    }

    // This function will update the widget when the location is changed
    private fun updateWidget(){
        val applicationContext = requireContext().applicationContext

        val intent = Intent(requireContext(), SkySphereWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(ComponentName(applicationContext, SkySphereWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        requireContext().sendBroadcast(intent)
    }

    private fun saveLocationToPrefs(latitude: Double, longitude: Double) {
        val sharedPrefs = requireContext().getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putFloat("latitude", latitude.toFloat())
            putFloat("longitude", longitude.toFloat())
            apply()
        }
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
