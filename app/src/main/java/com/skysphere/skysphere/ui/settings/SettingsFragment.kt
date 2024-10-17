package com.skysphere.skysphere.ui.settings

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.background_white)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.background_white))) // Action Bar Color

        // Change Status Bar Color
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.background_white) // Status Bar Color
        actionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle("")

        // Initializing a SharedPreferences object called sharedPreferences so that the user can access the
        // shared preferences, allowing you to read and write preferences (such as user settings or application state)
        // in a persistent storage

        // Assigning the buttons to their corresponding variables declared above
        val tempCard: CardView = view.findViewById(R.id.tempCard)
        val windCard: CardView = view.findViewById(R.id.windCard)
        val rainCard: CardView = view.findViewById(R.id.rainfallCard)
        val ttsCard: CardView = view.findViewById(R.id.ttsCard)
        val severeWeatherWarningCheckBox: CheckBox = view.findViewById(R.id.severe_weather_warnings)

        // Assigning the views to their corresponding variables declared above
        temperatureUnitTextView = view.findViewById(R.id.temp_details)
        windspeedUnitTextView = view.findViewById(R.id.wind_details)
        rainfallUnitTextView = view.findViewById(R.id.rain_details)
        ttsTextView = view.findViewById(R.id.tts_details)

        tempCard.setOnClickListener {
                val temperatureUnits = arrayOf("Celsius", "Fahrenheit")

                val currentUnit = settingsManager.getTemperatureUnit()
                val selectedIndex =
                    if (currentUnit == "Celsius") 0 else 1

                val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

                builder.setTitle("Temperature Unit")
                    .setSingleChoiceItems(temperatureUnits, selectedIndex) { dialog, which ->
                        when (which) {
                            0 -> {
                                // User selected Celsius
                                settingsManager.setPreferredUnit("temperature_unit", "Celsius")
                            }
                            1 -> {
                                // User selected Fahrenheit
                                settingsManager.setPreferredUnit("temperature_unit", "Fahrenheit")
                            }
                        }
                        updateTemperatureUnitTextView()
                        viewModel.fetchWeatherData()
                        dialog.dismiss()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
        }

        windCard.setOnClickListener {
            val windSpeedUnits = arrayOf("km/h", "mph", "m/s", "knots")
            val currentUnit = settingsManager.getWindSpeedUnit()

            val selectedIndex = when (currentUnit) {
                "km/h" -> 0
                "mph" -> 1
                "m/s" -> 2
                "knots" -> 3
                else -> 0
            }

            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

            builder.setTitle("Wind Speed Unit")
                .setSingleChoiceItems(windSpeedUnits, selectedIndex) { dialog, which ->
                    // Handle selection
                    when (which) {
                        0 -> {
                            settingsManager.setPreferredUnit("wind_speed_unit", "km/h")
                        }
                        1 -> {
                            settingsManager.setPreferredUnit("wind_speed_unit", "mph")
                        }
                        2 -> {
                            settingsManager.setPreferredUnit("wind_speed_unit", "m/s")
                        }
                        3 -> {
                            settingsManager.setPreferredUnit("wind_speed_unit", "knots")
                        }
                    }
                    updateWindSpeedUnitTextView()
                    viewModel.fetchWeatherData()
                    dialog.dismiss()
                }

            // Create and show the dialog
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        rainCard.setOnClickListener {
            val rainfallUnits = arrayOf("mm", "in.")

            val currentUnit = settingsManager.getRainfallUnit()
            val selectedIndex =
                if (currentUnit == "mm") 0 else 1

            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

            builder.setTitle("Rainfall Unit")
                .setSingleChoiceItems(rainfallUnits, selectedIndex) { dialog, which ->
                    when (which) {
                        0 -> {
                            settingsManager.setPreferredUnit("rainfall_unit", "mm")
                        }
                        1 -> {
                            settingsManager.setPreferredUnit("rainfall_unit", "in.")
                        }
                    }
                    updateRainfallUnitTextView()
                    viewModel.fetchWeatherData()
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        ttsCard.setOnClickListener {
            val ttsStatus = arrayOf("Enable", "Disable")

            val currentStatus = settingsManager.getTtsStatus()
            val selectedIndex =
                if (currentStatus == "enabled") 0 else 1

            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

            builder.setTitle("Enable Text To Speech")
                .setSingleChoiceItems(ttsStatus, selectedIndex) { dialog, which ->
                    when (which) {
                        0 -> {
                            settingsManager.setPreferredUnit("tts", "enabled")
                        }
                        1 -> {
                            settingsManager.setPreferredUnit("tts", "disabled")
                        }
                    }
                    updateTtsUnitTextView()
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        // Determining the checked state of the weather warnings notification preference (off by default)
        severeWeatherWarningCheckBox.isChecked = settingsManager.checkNotification(SEVERE_NOTIFICATION_PREFERENCE_KEY, false)

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
        val unit = settingsManager.getTemperatureSymbol()
        temperatureUnitTextView.text = unit
    }

    // Retrieving the stored preference for wind speed metric unit of the user
    private fun updateWindSpeedUnitTextView() {
        val unit = settingsManager.getWindSpeedUnit()
        windspeedUnitTextView.text = unit
    }

    // Retrieving the stored preference for rainfall metric unit of the user
    private fun updateRainfallUnitTextView() {
        val unit = settingsManager.getRainfallSymbol()
        rainfallUnitTextView.text = unit
    }

    // Retrieving the stored preference for rainfall metric unit of the user
    private fun updateTtsUnitTextView() {
        val status = settingsManager.getTtsString()
        ttsTextView.text = status
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

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.gradient_start))) // Action Bar Color

        // Change Status Bar Color
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_start) // Status Bar Color
    }
}