package com.skysphere.skysphere

import android.content.pm.PackageManager
import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.skysphere.skysphere.databinding.ActivityMainBinding
import com.skysphere.skysphere.notifications.WeatherService
import com.skysphere.skysphere.ui.settings.SettingsFragment
import com.skysphere.skysphere.background.WeatherUpdateWorker
import com.skysphere.skysphere.calendar.CalendarManager
import com.skysphere.skysphere.data.ConversionHelper
import com.skysphere.skysphere.data.SettingsManager
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var calendarManager: CalendarManager

    @Inject
    lateinit var viewModel: WeatherViewModel

    @Inject
    lateinit var settingsManager: SettingsManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Shared View Model with weather data stored in local database
        viewModel.fetchWeatherData()

        // Check if the data has been refreshed in the last 90 minutes
        if (shouldRunWorker(applicationContext)) {
            val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                repeatInterval = 90,    // Set worker interval to 90 minutes
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
            ).setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofMinutes(15) // Retry in 15 minutes if needed
            )
                .build()

            val workManager = WorkManager.getInstance(applicationContext)

            workManager.enqueueUniquePeriodicWork(
                "WeatherUpdateWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        } else {
            Log.d(
                "WeatherWorker",
                "Worker Ignored."
            ) // If work has already been run within 90 minutes, do not enqueue a new job
        }

        setSupportActionBar(binding.appBarMain.toolbar)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Initialize settings button
        val settingsButton = binding.appBarMain.toolbar.findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            // Navigate to the settings
            navController.navigate(R.id.nav_settings)
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_locations,
                R.id.nav_recommendations,
                R.id.nav_login,
                R.id.nav_logout,
                R.id.nav_news,
                R.id.nav_add_friend,
                R.id.nav_friends_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Create an on click listener for the log out item
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    auth.signOut()
                    updateNavigationMenu(false)
                    navController.navigate(R.id.nav_login)
                    true // Indicate that the logout was handled
                }

                else -> {
                    // Allow default navigation behavior for other items
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after item selection
                    false // Indicate that the item click was not handled here
                }
            }
        }

        // Set login flag to false
        updateNavigationMenu(false)
        updateNavigationMenu(auth.currentUser != null)

        // Initialize CalendarManager
        calendarManager = CalendarManager(this)

        // Check for calendar permission before accessing calendar
        checkCalendarPermissions()

        // Check for permissions and start notification service
        checkAndRequestNotificationPermission()

        // Fetch and update calendar events
        updateCalendarEvents()
    }

    // Method to update calendar events
    fun updateCalendarEvents() {
        setupObservers()
    }

    private fun setupObservers() {
        // Creating a preference key for weather event for calendar
        val eventAddedKey = "event_added"

        viewModel.weatherResults.observe(this) { weatherResults ->
            weatherResults?.let {
                if (it.daily == null || it.daily.roundedTemperatureMax.isEmpty() || it.daily.roundedTemperatureMin.isEmpty()) {
                    Log.e("WeatherData", "Weather data not available")
                }

                val sharedPreferences = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

                // Check the last added date
                val lastAddedDate = sharedPreferences.getLong("last_added_date", 0L)
                val currentDate = System.currentTimeMillis()

                // Reset the flag if it's a new day
                if (lastAddedDate == 0L || !calendarManager.isSameDay(lastAddedDate, currentDate)) {
                    with(sharedPreferences.edit()) {
                        putBoolean(eventAddedKey, false)
                        putLong("last_added_date", currentDate)
                        apply()
                    }
                }

                // Loop through the next 7 days
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // Loop through the next 6 days
                for (i in 0 until 7) {
                    // Increment day
                    val dayCalendar = Calendar.getInstance()
                    dayCalendar.timeInMillis = calendar.timeInMillis
                    dayCalendar.add(Calendar.DAY_OF_MONTH, i)

                    // Get the name of the day of the week
                    val weekDay = dayCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: "Day"

                    // Title updated to include the specific day
                    val title = "Weather for $weekDay"

                    // Getting temperature from Weather Repository
                    val temperature = it.current?.temperature

                    // Convert precipitation and wind speed using the ConversionHelper
                    val convertedTemperature = ConversionHelper.convertTemperature(temperature, settingsManager.getTemperatureUnit())

                    // Format wind speed to one decimal place
                    val formattedTemperature = String.format("%.0f", convertedTemperature ?: 0.0)

                    val dailyData = it.daily
                    if (dailyData != null && dailyData.roundedTemperatureMax.size > i) {
                        val daymax = dailyData.roundedTemperatureMax[i]
                        val daymin = dailyData.roundedTemperatureMin[i]
                        val precipitation = dailyData.precipitationSum[i] ?: 0.0 // Handle nulls
                        val windSpeed = dailyData.windSpeed[i] ?: 0.0 // Handle nulls

                        // Convert precipitation and wind speed using the ConversionHelper
                        val convertedPrecipitation = ConversionHelper.convertPrecipitation(precipitation, settingsManager.getPrecipitationUnit())
                        val convertedWindSpeed = ConversionHelper.convertWindSpeed(windSpeed, settingsManager.getWindSpeedUnit())

                        // Format wind speed to one decimal place
                        val formattedWindSpeed = String.format("%.1f", convertedWindSpeed ?: 0.0)

                        // Update the description with all relevant data
                        val description: String = if (calendarManager.isSameDay(dayCalendar.timeInMillis, System.currentTimeMillis())) {
                            "Current Temperature: $formattedTemperature ${settingsManager.getTemperatureSymbol()}\n" +
                                    "Expected Temperatures:\n" +
                                    "   Max: $daymax ${settingsManager.getTemperatureSymbol()} & Min: $daymin ${settingsManager.getTemperatureSymbol()}\n" +
                                    "Precipitation: $convertedPrecipitation mm\n" +
                                    "Wind Speed: $formattedWindSpeed ${settingsManager.getWindSpeedUnit()}\n"
                        } else {
                            "Expected Temperatures:\n" +
                                    "   Max: $daymax ${settingsManager.getTemperatureSymbol()} & Min: $daymin ${settingsManager.getTemperatureSymbol()}\n" +
                                    "Precipitation: $convertedPrecipitation mm\n" +
                                    "Wind Speed: $formattedWindSpeed ${settingsManager.getWindSpeedUnit()}\n"
                        }

                        // Start of the day
                        val startTime = dayCalendar.apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        // End of the day
                        dayCalendar.add(Calendar.DAY_OF_MONTH, 1)
                        val endTime = dayCalendar.timeInMillis // End of the day

                        val calendarId = calendarManager.getCalendarId()

                        if (calendarId != null) {
                            calendarManager.addWeatherEvent(title, description, startTime, endTime, calendarId)
                        } else {
                            Log.e("No calendar found", "Calendar Permission was denied.")
                        }
                    } else {
                        Log.e("WeatherData", "Daily data not available or insufficient data.")
                    }
                }
            }
        }
    }

    // Function to check if the worker should be ran
    private fun shouldRunWorker(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val lastExecutionTime = sharedPreferences.getLong("last_execution_time", 0L)
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastExecutionTime

        // Return true if more than 90 minutes have passed
        val result = elapsedTime >= 90 * 60 * 1000
        Log.d("WeatherWorkerTime", "Time since last call (ms): ${elapsedTime}, boolean = ${result}")
        return result
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Function to swap the nav menu depending on if the user is logged in or not
    fun updateNavigationMenu(isLoggedIn: Boolean) {

        val navView: NavigationView = binding.navView
        navView.menu.clear()
        if (!isLoggedIn) {
            navView.inflateMenu(R.menu.activity_main_drawer)
        } else {
            navView.inflateMenu(R.menu.login_drawer)
        }
    }

    // Code for getting calendar permissions
    private val CALENDAR_PERMISSIONS_REQUEST_CODE = 100

    //Storing preference of calendar permission being denied
    private val PREF_CALENDAR_PERMISSION_DENIED = "calendar_permission_denied"

    // Permissions request for calendar
    private fun checkCalendarPermissions() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isCalendarPermissionDenied = sharedPreferences.getBoolean(PREF_CALENDAR_PERMISSION_DENIED, false)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            if (isCalendarPermissionDenied) {
                // User has previously denied permission, handle accordingly (e.g., show a message to the user)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
                    CALENDAR_PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            handleCalendarOperations() // Permissions granted, handle calendar operations
        }
    }

    //Code for getting notification permissions
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 123

    //Checks to see if notification permissions are granted and start weather service if they are.
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                startWeatherServiceIfEnabled()
            }
        } else {
            startWeatherServiceIfEnabled()
        }
    }

    // Handles the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALENDAR_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    handleCalendarOperations() // Permissions granted
                } else {
                    // User denied permissions
                    Log.d("CalendarPermissions", "User denied calendar permissions; skipping calendar operations.")
                    Toast.makeText(this, "Calendar features are disabled due to lack of permissions.", Toast.LENGTH_SHORT).show()

                    // Store that the user has denied permission
                    val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putBoolean(PREF_CALENDAR_PERMISSION_DENIED, true)
                        apply()
                    }
                }
            }
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted, start the weather service for notifications
                    startWeatherServiceIfEnabled()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message to the user)
                }
            }
        }
    }

    // Handle calendar operations after permission is granted
    private fun handleCalendarOperations() {
        // Check if permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // Proceed with calendar operations
            calendarManager.getCalendarId()
            setupObservers()
        } else {
            // Log and notify user that calendar features are disabled
            Log.d("CalendarOperations", "Calendar permissions not granted; skipping calendar operations.")
            // You can also disable related UI elements here if needed
        }
    }

    // Starts the weather service if notification permissions are granted
    private fun startWeatherServiceIfEnabled() {

        // Only start the service if it is not already running and at least 1 notification option is selected
        if (!isServiceRunning(WeatherService::class.java)) {
            if (settingsManager.checkNotification(SettingsFragment.SEVERE_NOTIFICATION_PREFERENCE_KEY, true) ||
                settingsManager.checkNotification(SettingsFragment.RAIN_FORECAST_NOTIFICATION_PREFERENCE_KEY, true) ||
                settingsManager.checkNotification(SettingsFragment.DAILY_SUMMARY_NOTIFICATION_PREFERENCE_KEY, true)) {
                WeatherService.startWeatherMonitoring(this)
            }
        } else {
            Log.d("WeatherService", "Service is already running, no need to start.")
        }
    }

    // Function to check if the WeatherService is running
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}