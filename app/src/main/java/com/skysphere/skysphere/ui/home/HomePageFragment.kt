package com.skysphere.skysphere.ui.home

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.skysphere.skysphere.R
import com.skysphere.skysphere.GPSManager
import java.util.Locale
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.speech.tts.TextToSpeech
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.BackoffPolicy
import androidx.work.WorkManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import com.airbnb.lottie.LottieAnimationView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.skysphere.skysphere.view_models.WeatherViewModel
import com.skysphere.skysphere.background.WeatherUpdateWorker
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.repositories.WeatherRepository
import com.skysphere.skysphere.data.WeatherResults
import com.skysphere.skysphere.data.repositories.LocationRepository
import com.skysphere.skysphere.ui.adapters.DailyWeatherAdapter
import com.skysphere.skysphere.widgets.SkySphereWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class HomePageFragment : Fragment(), GPSManager.GPSManagerCallback,
    SwipeRefreshLayout.OnRefreshListener {

    /*
        Inject Hilt components
     */
    @Inject
    lateinit var viewModel: WeatherViewModel

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var locationRepository: LocationRepository

    private var weatherResults: WeatherResults? = null
    private lateinit var dailyWeatherAdapter: DailyWeatherAdapter
    private lateinit var dailyRecyclerView: RecyclerView

    // Declare the views that have been created in the XML file.
    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var weatherCodeAnimationView: LottieAnimationView
    private lateinit var temperatureTextView: TextView
    private lateinit var temperatureUnit: TextView
    private lateinit var feelsLikeTemperatureTextView: TextView
    private lateinit var weatherStateTextView: TextView
    private lateinit var homeTextView: TextView
    private lateinit var lastUpdatedText: TextView
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var textToSpeechBtn: ImageButton
    private lateinit var hourlyCardView: CardView
    private lateinit var dailyCardView: CardView
    private lateinit var currentLocationButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // Declaring the clickable upper region and the variables that will inside the alertbox.
    private lateinit var upperRegion: FrameLayout

    // Declaring chart which will be used to display hourly temperatures
    private lateinit var temperatureChart: LineChart


    // Declare the GPS Manager class that uses the user's location.
    private lateinit var gpsManager: GPSManager

    // Declare the shared preferences that stores the metric units
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set FirstOpened to false (this is used to detect if UI animations should play)
        settingsManager.saveFirstOpened(false)

        // Observe live data from the shared view model
        viewModel.weatherResults.observe(this) { results ->
            weatherResults = results
            setData() // Set UI data
        }
    }

    // Sets the data retrieved from the API to the views declared at the beginning.
    private fun setData() {

        weatherResults?.let {

            // Current
            // Animate temperature
            var isFirstOpen = settingsManager.isFirstOpened()
            if (!isFirstOpen) {
                animateTemperature(0, it.current?.roundedTemperature)
                settingsManager.saveFirstOpened(true)
            } else {
                temperatureTextView.text = it.current?.roundedTemperature.toString()
            }
            temperatureUnit.text = it.current?.tempUnit
            weatherStateTextView.text = it.current?.weatherText

            // Play weather type animation
            it.current?.weatherType?.let { weatherType ->
                weatherType.lottieAnimRes?.let { lottieFileName ->

                    weatherCodeAnimationView.alpha = 0f
                    val fadeIn = ObjectAnimator.ofFloat(weatherCodeAnimationView, "alpha", 0f, 1f)
                    fadeIn.duration = 500
                    fadeIn.start()
                    weatherCodeAnimationView.setAnimation(lottieFileName)
                    weatherCodeAnimationView.playAnimation()
                }
            }

            feelsLikeTemperatureTextView.text =
                "Feels like " + it.current?.roundedApparentTemperature.toString() + "°"
            dateTextView.text = it.current?.date
            locationTextView.text = settingsManager.getCustomLocation()
            lastUpdatedText.text = it.current?.updatedTime

            // Weekly
            dailyWeatherAdapter = DailyWeatherAdapter(weatherResults?.daily) { position ->
                // Handle the onclick
                val bundle = Bundle().apply {
                    putInt("clickedPosition", position)
                }

                // Navigate to the DailyDetailsFragment
                val navController = findNavController()
                navController.navigate(R.id.action_nav_daily_details, bundle)

            }

            dailyRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = dailyWeatherAdapter
            }

            // Hourly
            val times = List(24) { index -> String.format("%02d:00", index) }
            setupTemperatureChart(it.hourly?.temperature?.take(24), times)
        } ?: run {

        }
        updateWidget()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        updateColours()

        dailyRecyclerView = view.findViewById((R.id.dailyRecycler))

        // Assign the views to variables declared above.
        dateTextView = view.findViewById(R.id.tvDate)
        locationTextView = view.findViewById(R.id.tvLocation)
        weatherCodeAnimationView = view.findViewById((R.id.weatherCodeAnimationView))
        temperatureTextView = view.findViewById(R.id.tvTemperature)
        temperatureUnit = view.findViewById(R.id.tvTemperatureUnit)
        feelsLikeTemperatureTextView = view.findViewById(R.id.tvFeelsLikeTemperature)
        weatherStateTextView = view.findViewById(R.id.tvWeatherState)
        homeTextView = view.findViewById(R.id.text_home)
        textToSpeechBtn = view.findViewById(R.id.ttsBtn)
        lastUpdatedText = view.findViewById(R.id.tvLastUpdated)
        hourlyCardView = view.findViewById(R.id.cvHourly)
        dailyCardView = view.findViewById(R.id.daily_card)
        currentLocationButton = view.findViewById(R.id.currentLocationButton)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener(this)

        // Initializing the show more details functionality
        upperRegion = view.findViewById(R.id.upperRegion)

        // Clickable region to show wind details in an alert dialog
        upperRegion.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_nav_current_details)
        }

        // Play card view animation if required
        var isFirstOpen = settingsManager.isFirstOpened()
        if (!isFirstOpen) {
            fadeInCardViews(listOf(hourlyCardView, dailyCardView))
        }


        // Initializing the users preferences
        sharedPreferences =
            requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Initializing the chart
        temperatureChart = view.findViewById(R.id.temperatureChart)

        // GPS client
        gpsManager = GPSManager(requireContext())

        if (settingsManager.getTtsStatus() == "enabled") {
            textToSpeechBtn.visibility = View.VISIBLE
        }

        // Text to speech button
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(requireContext(), "Language not supported", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
        // When clicked AI will speak to user
        textToSpeechBtn.setOnClickListener {
            textToSpeechDialog()
        }

        // Update the current location of the device
        getLocation()

        return view
    }

    private fun updateColours() {
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_start)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_end)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gradient_start
                )
            )
        )
    }

    // Text for text to speech
    private fun textToSpeechDialog() {
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

    private fun refreshWeather() {

        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true

        if (!isConnected) {
            // Show a Snackbar if there is no internet connection
            view?.let { Snackbar.make(it, "Network Unavailable", Snackbar.LENGTH_SHORT).show() }
        }

        val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
            repeatInterval = 90,    // Set worker interval to 90 minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        ).setBackoffCriteria(
            backoffPolicy = BackoffPolicy.LINEAR,
            duration = Duration.ofMinutes(15) // Retry in 15 minutes if needed
        )
            .build()

        val workManager = WorkManager.getInstance(requireContext())

        workManager.enqueueUniquePeriodicWork(
            "WeatherUpdateWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
        settingsManager.saveFirstOpened(false)
    }

    private fun setupTemperatureChart(temperatures: List<Double?>?, times: List<String>) {
        // Filter out null values
        val nonNullTemperatures = temperatures?.filterNotNull() ?: emptyList()

        // Creating entry points for line chart
        val entries = nonNullTemperatures.mapIndexed { index, temp ->
            Entry(index.toFloat(), temp.toFloat())
        }

        // Dataset custom appearance
        val dataSet = LineDataSet(entries, "Hourly Temperature")
        dataSet.valueTextColor = Color.WHITE
        dataSet.lineWidth = 2f
        dataSet.setColor(Color.WHITE)
        dataSet.setCircleColor(Color.WHITE)
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 12f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.BLACK
        dataSet.fillAlpha = 50
        dataSet.valueFormatter = DefaultValueFormatter(0)
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
        xAxis.setDrawAxisLine(false)
        xAxis.gridColor = 1

        // Y-axis configuration
        val leftAxis = temperatureChart.axisLeft
        temperatureChart.axisRight.isEnabled = false
        leftAxis.textColor = Color.WHITE
        leftAxis.xOffset = 16f
        leftAxis.setDrawAxisLine(false)

        // Making chart scrollable
        temperatureChart.setVisibleXRangeMaximum(5f)
        temperatureChart.moveViewToX(0f)

        // Refreshing the chart
        temperatureChart.invalidate()
    }

    // Gets the user location by making user accept location permissions
    private fun getLocation() {
        // This if statement checks if user has granted user location permissions (fine location and coarse location).
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // This statement occurs when permissions haven't been granted, and sends the request to the user.
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // If the location permission is granted, then it will attempt to get the last location of the user from GPS Manager.
        gpsManager.getCurrentLocation(this)
    }


    override fun onLocationRetrieved(
        latitude: Double,
        longitude: Double,
        locality: String?,
        country: String?
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Insert current location into local database
            locationRepository.saveCurrentLocation(locality, country, latitude, longitude)
        }
    }

    override fun onLocationError(error: String) {
        locationTextView.text = error
    }

    // Used to identify permission request.
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    // This function will update the widget when the location is changed
    private fun updateWidget() {
        val applicationContext = requireContext().applicationContext

        val intent = Intent(requireContext(), SkySphereWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(ComponentName(applicationContext, SkySphereWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        requireContext().sendBroadcast(intent)
    }

    // Handles when the user grants or denies location permissions.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If user denies then it sends the request again. If user denies again then a message is shown to inform that permissions must be allowed.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                } else {
                    locationTextView.text = "You must allow location permission to get weather data"
                }
                return
            }
        }
    }

    // Function to animate the temperature
    private fun animateTemperature(startValue: Int, endValue: Int?) {
        // If value is not null, run animation
        if (endValue == null) return

        // Calculate the difference between start and end values
        val difference = Math.abs(endValue - startValue)

        // Set the duration based on the difference
        val duration =
            if (difference < 20) 1000L else 2000L // 1 second for small differences, 2 seconds for larger

        // Create a ValueAnimator
        val animator = ValueAnimator.ofInt(startValue, endValue)
        animator.duration = duration // Use the calculated duration

        // Add an update listener to update the TextView
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            temperatureTextView.text =
                "$animatedValue" // Update the TextView with the animated value
        }

        // Start the animation
        animator.start()
    }

    // Function to animate the card views one by one
    private fun fadeInCardViews(cardViews: List<CardView>) {
        for ((index, cardView) in cardViews.withIndex()) {
            // Set the initial alpha to 0 (completely transparent)
            cardView.alpha = 0f
            cardView.visibility = View.VISIBLE // Make sure the CardView is visible

            // Animate the fade-in with a delay based on the index
            cardView.animate()
                .alpha(1f)
                .setDuration(1500) // Duration for the fade-in
                .setStartDelay(index * 800L) // Staggered delay (300ms for each CardView)
                .start()
        }
    }

    override fun onStart() {
        super.onStart()
        updateColours()
        setData()
    }

    override fun onRefresh() {
        refreshWeather()
        swipeRefreshLayout.isRefreshing = false
    }
}
