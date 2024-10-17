package com.skysphere.skysphere.ui.settings

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.skysphere.skysphere.R
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.databinding.FragmentDetailsBinding
import com.skysphere.skysphere.notifications.WeatherService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class SettingsFragment : Fragment()
{
    // Initializing variables to store user preferences
    @Inject
    lateinit var viewModel: WeatherViewModel
    @Inject
    lateinit var settingsManager: SettingsManager


    // Object for severe notification preference key, needed for the notification functionality
    companion object {
        const val SEVERE_NOTIFICATION_PREFERENCE_KEY = "severe_notification_preference"
    }

    // Declared the views that have been created in the XML files
    private lateinit var temperatureUnitTextView: TextView
    private lateinit var windspeedUnitTextView: TextView
    private lateinit var rainfallUnitTextView: TextView
    private lateinit var ttsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.card_white)

        // Initializing a SharedPreferences object called sharedPreferences so that the user can access the
        // shared preferences, allowing you to read and write preferences (such as user settings or application state)
        // in a persistent storage

        // Assigning the buttons to their corresponding variables declared above
        val celsiusButton: Button = view.findViewById(R.id.Celsius)
        val fahrenheitButton: Button = view.findViewById(R.id.Fahrenheit)
        val metersPerSecondButton: Button = view.findViewById(R.id.Mps)
        val kilometersPerHourButton: Button = view.findViewById(R.id.Km_h)
        val milesPerHourButton: Button = view.findViewById(R.id.Mph)
        val knotsButton: Button = view.findViewById(R.id.Knots)
        val millimetersButton: Button = view.findViewById(R.id.Millimeter)
        val inchesButton: Button = view.findViewById(R.id.Inches)
        val ttsEnableButton: Button = view.findViewById(R.id.tts_enable)
        val ttsDisableButton: Button = view.findViewById(R.id.tts_disable)
        val severeWeatherWarningCheckBox: CheckBox = view.findViewById(R.id.severe_weather_warnings)

        // Assigning the views to their corresponding variables declared above
        temperatureUnitTextView = view.findViewById(R.id.temp_details)
        windspeedUnitTextView = view.findViewById(R.id.wind_speed_details)
        rainfallUnitTextView = view.findViewById(R.id.rainfall_details)
        ttsTextView = view.findViewById(R.id.tts_details)

        // Determining the checked state of the weather warnings notification preference (off by default)
        severeWeatherWarningCheckBox.isChecked = settingsManager.checkNotification(SEVERE_NOTIFICATION_PREFERENCE_KEY, false)

        // Setting up the buttons in the settings fragment xml file with their corresponding metric unit
        celsiusButton.setOnClickListener {
            settingsManager.setPreferredUnit("temperature_unit","Celsius")
            updateTemperatureUnitTextView()
            viewModel.fetchWeatherData()
        }
        fahrenheitButton.setOnClickListener{
            settingsManager.setPreferredUnit("temperature_unit","Fahrenheit")
            updateTemperatureUnitTextView()
            viewModel.fetchWeatherData()
        }
        metersPerSecondButton.setOnClickListener {
            settingsManager.setPreferredUnit("wind_speed_unit","m/s")
            updateWindSpeedUnitTextView()
            viewModel.fetchWeatherData()
        }
        kilometersPerHourButton.setOnClickListener{
            settingsManager.setPreferredUnit("wind_speed_unit","kmh")
            updateWindSpeedUnitTextView()
            viewModel.fetchWeatherData()
        }
        milesPerHourButton.setOnClickListener{
            settingsManager.setPreferredUnit("wind_speed_unit","mph")
            updateWindSpeedUnitTextView()
            viewModel.fetchWeatherData()
        }
        knotsButton.setOnClickListener{
            settingsManager.setPreferredUnit("wind_speed_unit","knots")
            updateWindSpeedUnitTextView()
            viewModel.fetchWeatherData()
        }
        millimetersButton.setOnClickListener {
            settingsManager.setPreferredUnit("rainfall_unit","millimeters")
            updateRainfallUnitTextView()
            viewModel.fetchWeatherData()
        }
        inchesButton.setOnClickListener {
            settingsManager.setPreferredUnit("rainfall_unit","inches")
            updateRainfallUnitTextView()
            viewModel.fetchWeatherData()
        }
        ttsEnableButton.setOnClickListener {
            settingsManager.setPreferredUnit("tts","enabled")
            updateTtsUnitTextView()
        }
        ttsDisableButton.setOnClickListener {
            settingsManager.setPreferredUnit("tts","disabled")
            updateTtsUnitTextView()
        }
        // Setting up the listener for the severe weather warnings checkbox
        severeWeatherWarningCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkNotificationPermission()) {
                    settingsManager.saveNotificationPreference(true)
                    WeatherService.startWeatherMonitoring(requireContext())
                } else {
                    // Uncheck the box if permission is not granted
                    severeWeatherWarningCheckBox.isChecked = false
                    Toast.makeText(context, "Notification permission is required for severe weather alerts", Toast.LENGTH_LONG).show()
                }
            } else {
                settingsManager.saveNotificationPreference(false)
                WeatherService.stopWeatherMonitoring(requireContext())
            }
        }

        // Initializing the TextView of Temperature Details with the current metric unit
        updateTemperatureUnitTextView()
        // Initializing the TextView of Wind Speed Details with the current metric unit
        updateWindSpeedUnitTextView()
        // Initializing the TextView of Rainfall Details with the current metric unit
        updateRainfallUnitTextView()
        // Initializing the TextView of TTS Details with the current status
        updateTtsUnitTextView()

        return view
    }

    // Retrieving the stored preference for temperature metric unit of the user
    private fun updateTemperatureUnitTextView() {
        // This sets the TextView for the Temperature Details to what it equates and displays it onto the settings page
        val unit = settingsManager.getTemperatureUnit()
        temperatureUnitTextView.text = "The temperature unit is currently set to " + unit
    }

    // Retrieving the stored preference for wind speed metric unit of the user
    private fun updateWindSpeedUnitTextView() {
        val unit = settingsManager.getWindSpeedUnit()
        windspeedUnitTextView.text = "The wind speed unit is currently set to " + unit
    }

    // Retrieving the stored preference for rainfall metric unit of the user
    private fun updateRainfallUnitTextView() {
        val unit = settingsManager.getRainfallUnit()
        rainfallUnitTextView.text = "The rainfall unit is currently set to " + unit
    }

    // Retrieving the stored preference for rainfall metric unit of the user
    private fun updateTtsUnitTextView() {
        val status = settingsManager.getTtsStatus()
        ttsTextView.text = "Text to speech is currently " + status
    }

    // Checking if the notification permission has been granted
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}