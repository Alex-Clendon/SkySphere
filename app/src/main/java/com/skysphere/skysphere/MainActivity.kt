package com.skysphere.skysphere

import android.content.pm.PackageManager
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
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
import com.skysphere.skysphere.data.SettingsManager
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.util.Calendar
import java.util.TimeZone
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
        val currentUnit = settingsManager.getTemperatureUnit()
        setupObservers(currentUnit)
    }

    private fun setupObservers(currentUnit: String) {
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

                // Get the current date
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // Loop through the next 6 days
                for (i in 0 until 6) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_MONTH, i) // Increment day

                    val title = "Weather for the Day"

                    // Get the preferred temperature unit from SharedPreferences
                    val temperature = if (currentUnit == "Celsius") {
                        it.current?.roundedTemperature ?: "N/A" // Fallback
                    } else {
                        // Convert to Fahrenheit
                        (it.current?.roundedTemperature?.toDouble()?.times(9/5)?.plus(32))?.toString() ?: "N/A" // Fallback
                    }
                    val unit = settingsManager.getTemperatureSymbol()

                    val dailyData = it.daily
                    if (dailyData != null && dailyData.roundedTemperatureMax.size > i && dailyData.roundedTemperatureMin.size > i) {
                        val daymax = dailyData.roundedTemperatureMax[i]
                        val daymin = dailyData.roundedTemperatureMin[i]

                        // Add event to calendar...
                        val description: String = if (calendarManager.isSameDay(calendar.timeInMillis, System.currentTimeMillis())) {
                            // If it's today, include current weather
                            "Current Temperature: $temperature $unit\n" +
                                    "Expected Temperatures:\n" +
                                    "Max: $daymax $unit & Min: $daymin $unit"
                        } else {
                            // If it's not today, only show expected weather
                            "Expected Temperatures:\n" +
                                    "Max: $daymax $unit & Min: $daymin $unit"
                        }

                        // Start of the day
                        val startTime = calendar.apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        // End of the day
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        val endTime = calendar.timeInMillis // End of the day

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
        Log.d("WeatherWorkerTime", "Time since last call (ms): ${elapsedTime}, boolean = $result")
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

    // Handle permission result
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
            val currentUnit = settingsManager.getTemperatureUnit()
            calendarManager.getCalendarId()
            setupObservers(currentUnit)
        } else {
            // Log and notify user that calendar features are disabled
            Log.d("CalendarOperations", "Calendar permissions not granted; skipping calendar operations.")
            Toast.makeText(this, "Calendar features are disabled due to lack of permissions.", Toast.LENGTH_SHORT).show()
            // You can also disable related UI elements here if needed
        }
    }

    // Starts the weather service if notification permissions are granted
    private fun startWeatherServiceIfEnabled() {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isNotificationEnabled =
            sharedPreferences.getBoolean(SettingsFragment.SEVERE_NOTIFICATION_PREFERENCE_KEY, false)
        if (isNotificationEnabled) {
            WeatherService.startWeatherMonitoring(this)
        }
    }
}